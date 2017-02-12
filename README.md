# README #
## Launch ##
Start the application with `sbt "run ./data/data_scala.txt"`.

Or use `io.koff.timeseries.Main` as the entry point.

## Description ##

There are several implementations of the necessary calculations:

* `io.koff.timeseries.naive.NaiveCalculator` - a very simple but correct implementation of calculations. It also helps to validate the optimized version of the algorithm.
* **`io.koff.timeseries.optimized.OptimizedCalculator`** - an attempt to implement of different enhancements and optimizations. This is **the main version** of the algorithm that is used in `io.koff.timeseries.Main`

I have started implementing this program with `NaiveCalculator` where I used a straightforward approach in order to understand possible problems. After that I have desided to try to add different enhancements and optimizations. In such a way I have got `OptimizedCalculator`. 

There are two things in `OptimizedCalculator` that I would like to bring to notice:

* Usage of SeqView - lazy version of collections. It is a convenient way to avoid unnecessary copying of data between different collections during calculations.
* Reduction of number of operations for calculation of a rolling sum, number of measurements, min and max values. The idea is to use inverse direction of traversal through data in  the buffer and to reuse information from a previous element. Please look at this [picture](http://i.imgur.com/kGU0jdg.jpg).

## Assumptions ##
* Measurements are sorted by time from the earliest to the latest - this is crucial for the current algorithm.
* All measurements in a rolling window can be placed in RAM - it is important because only input records in memory buffer are used for analisys. In other case we need to use external memory algorithms.
* Values of measurements are evenly distributed - it helps to reduce operations to find min/max values at the average. But there are other possible options. For example, if values of measurements increase(or decrease) monotonically then it is possible to optimize this part even better.
* All input data is placed in one file.