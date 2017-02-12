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
* Reduction of number of operations for calculation of a rolling sum, number of measurements, min and max values. The idea is to use inverse direction of traversal of data in buffer and reusage of information from a previous element. Look at this [picture](http://i.imgur.com/kGU0jdg.jpg).