# README #
## Launch ##
Start the application with `sbt "run ./data/data_scala.txt"`.

Or use `io.koff.timeseries.Main` as the entry point.

Example of output:
```
T          V       N   RS        MinV    MaxV
------------------------------------------------
1355270609 1,80215 1   1,80215   1,80215 1,80215
1355270621 1,80185 2   3,604     1,80185 1,80215
1355270646 1,80195 3   5,40595   1,80185 1,80215
1355270702 1,80225 2   3,6042    1,80195 1,80225
1355270702 1,80215 3   5,40635   1,80195 1,80225
1355270829 1,80235 1   1,80235   1,80235 1,80235
1355270854 1,80205 2   3,6044    1,80205 1,80235
1355270868 1,80225 3   5,40665   1,80205 1,80235
1355271000 1,80245 1   1,80245   1,80245 1,80245
1355271023 1,80285 2   3,6053    1,80245 1,80285
1355271024 1,80275 3   5,40805   1,80245 1,80285
1355271026 1,80285 4   7,2109    1,80245 1,80285
```

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