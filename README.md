
# Backtester

## Check out the visualization tool I built for this -- https://github.com/prcao/backtester-viz

Simple backtesting framework written in Java, powered by redis. ***Still in early development.***

![enter image description here](https://wompampsupport.azureedge.net/fetchimage?siteId=7575&v=2&jpgQuality=100&width=700&url=https://i.kym-cdn.com/entries/icons/facebook/000/029/959/Screen_Shot_2019-06-05_at_1.26.32_PM.jpg)

# Data

## Redis cache for storing historical market data

Information about a stock should be stored in a hash with the field being the stock ticker. Keys for each hash should be a date in the format YYYY-MM-DD.

Data is stored as a JSON string.

`> hget AMD 2020-01-02`
```
{
	"date": "2020-01-02",
	"open": 46.860001,
	"high": 49.25,
	"low": 46.630001,
	"close": 49.099998,
	"adjClose": 49.099998,
	"volume": 8.03311E7,
	"symbol": "AMD"
}
```
A list of open market days in the format YYYY-MM-DD should also be stored in the field `open_market_days`, with the unix timestamp as the scores.

`> zrange open_market_days 0 5`
```
1) "1979-07-16"
2) "1979-07-17"
3) "1979-08-17"
4) "1979-10-05"
5) "1979-12-03"
6) "1979-12-04"
```

## Backfilling redis cache with data

`BackfillDataScript.java` contains a script that populates a redis cache with historical stock market data, as well as open market days.

# Using the Framework

## Obtaining Data from Redis
### StockUtil
`StockUtil.getQuote(String ticker, String date)` should be used to get a historical quote. It returns a `StockInfo` object.

### StockInfo
`StockInfo` is just the Java object of the JSON stored in the redis cache.
```
public  class  StockInfo  {
	private  String symbol, date;
	private  double open, low, high, close, adjClose;
	private  long volume;
	// getters for everything...
}
```
## Logging
Logging is handled in `Logger.java`. By default, `Logger.log()` logs to `logs.txt` and `Logger.error()` logs to `errors.txt`

Simply `tail -f logs.txt` to view logs as the program runs.

## Running a Backtest

### PortfolioSnapshot
A portfolio snapshot is a snapshot in time of a portfolio's current state. It stores information about 
- Current positions -- `HashMap<String, Double> positions`
- Cash -- `double cash`
- Outstanding orders to be executed at the beginning of each new market day -- `List<MarketOrder> outstandingOrders`
- Date of snapshot -- `String date` -- in `YYYY-MM-DD` format, of course

`PortfolioSnapshot(Portfolio  portfolio, String  date, HashMap<String, Double> positions, double  cash)`

During construction, you need to specify the `Portfolio` that this snapshot is contained in. This is so that the `Portfolio` can record a history of market orders.

### MarketOrder
`LimitBuyOrder(PortfolioSnapshot snapshot, String ticker, double quantity, double limitPrice, String openDate [, String expirationDate])`

`LimitSellOrder(PortfolioSnapshot snapshot, String ticker, double quantity, double limitPrice, String openDate [, String expirationDate)`

`expirationDate` will default to the date of the snapshot, if not specified.

The `execute()` function will attempt to execute the order, updating the positions of the PortfolioSnapshot it is attached to, if successful. It will return `true` on success and `false` if the order was not able to execute.

#### Outstanding orders
At the beginning of each new day, a new snapshot will attempt to execute all of its outstanding orders from the previous snapshot. If an order is successful, it is removed from the snapshot's outstanding order list. An order will also be removed from the list if the expiration date has passed.

To queue an order, use the 

`PortfolioSnapshot.queueOrderAtOpen(MarketOrder order)` or

`PortfolioSnapshot.queueOrderEOD(MarketOrder order)`

 functions. `queueOrderAtOpen` will attempt to execute the order the day it is queued and add itself to the snapshot's outstanding orders if it fails, `queueOrderAtEOD` will simply add itself to the snapshot's outstanding orders.


### Strategy
A strategy defines a trading strategy to be executed every market day. It is a functional interface with the signature


`PortfolioSnapshot execute(PortfolioSnapshot  init, String  currentDatestamp)`


`init` is the state of the portfolio before the strategy is executed. This function should return the state of the portfolio after the strategy has been executed.

Here is what a simple do nothing strategy would look like:
```
Strategy holdStrategy = (init, date) -> new PortfolioSnapshot(init.portfolio, date, init.positiions, init.cash);
```
Here is a slightly more complicated strategy that will attempt to buy 100 shares of AMD on red days and will attempt to sell 100 shares of AMD on green days.
```
Strategy badStrategy = (PortfolioSnapshot snapshot, String date) -> {
	String stonk  =  "AMD";
	StockInfo stockInfo  =  StockUtil.getQuote(stonk, date);
	double open  =  stockInfo.getOpen();
	double close  =  stockInfo.getClose();

	PortfolioSnapshot eod = new PortfolioSnapshot(snapshot);

	if(close < open) {
		eod.queueOrderAtOpen(new LimitBuyOrder(eod, stonk, 100, close, date));
	} else {
		eod.queueOrderAtOpen(new LimitSellOrder(eod, stonk, 100, close, date));
	}
	
	return eod;
};
```
It is up to you, the programmer, to ensure that your strategy does not use data that you wouldn't have. For example, make sure you don't queue an order at market open depending on the highs and lows for the day.

### Portfolio
`Portfolio.java` constructs the data for a given strategy.
When filling a portfolio, you must specify a `PortfolioSnapshot` that represents the initial state we should execute our strategy on.


```
Portfolio p = new Portfolio(strategy);
p.backtest(initialSnapshot);
```

After the backtest is done running, we can  get the resulting data.

```
DataSet data = p.getData();
data.saveJSONToFile("results.txt");
data.prettyPrint();
```
#### Portfolio History

History of actions performed on a portfolio is stored in `List<MarketEvent>  history`
`MarketEvent` stores information about any action that was performed on the portfolio, including
- Opening an order
- Successfully executing an order
- Expiring an order
- Failing to execute an order

### TimeUtil
`TimeUtil` contains useful functions for navigating forward and backwards through open market days.
`String prevDay = TimeUtil.previousMarketDay("2020-06-08");`

### Market Scanner

Market scanner is a simple util class to scan the entire stock market. We pass in a `MarketScannerCondition` functional interface that simply returns a boolean. We use the condition to run a filter on every single ticker in the market (which is defined in a .txt file).

`List<String> scan(MarketScannerCondition condition, String date, boolean multithreading)`

If we wanted to get a list of stock tickers that started with the letter "A":
```
	String today = "2020-06-08";
	MarketScannerCondition condition = (ticker, date) -> ticker.charAt(0) == 'A';
	List<String> stonks = MarketScanner.scan(condition, today, true);
```

If we wanted to get a list of stock tickers whose volume is 100x the last market day's

```
	String today = "2020-06-08";
	MarketScannerCondition condition = (ticker, date) -> {
		StockInfo prevInfo = StockUtil.getQuote(ticker, TimeUtil.getPreviousMarketDay(date));
		StockInfo info = StockUtil.getQuote(ticker, date);

		return info.getVolume() > 100 * prevInfo.getVolume();
	}
	
	List<String> stonks = MarketScanner.scan(condition, today, true);
```

## Displaying data
A react webapp to visualize results and market history is in development.

## More to come later!
