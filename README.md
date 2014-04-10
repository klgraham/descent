# Descent

As projects grow in complexity, their dependencies often do as well. When release time comes, it may help to know how your various libraries depend on one another. Descent combines Clojure, Datomic (maybe), and [Rhizome](https://github.com/ztellman/rhizome) to track project dependencies over time.

Have yet to implement the Datomic storage. Will do that soon. For right now, the functionality is as follows:

  1. if you pass in a pom, you'll get a graph of all the dependencies. The graph will be written to a file.
  2. If you run again on a different pom, you'll get a diferent graph. You can then merge the two results to obtain a composite dependency graph.

## Installation

  1. Download from https://github.com/klgraham/descent.
  2. `cd` to the descent directory.
  3. Run `lein uberjar` if you want to run using a jar, then look at the Usage below.
  4. Run `lein run <args>`, if you don't want to use a jar. See Usage example for arguments.

## Usage

    $ java -jar descent-0.1.0-standalone.jar <directory with poms> <group-id prefix to filter on OR use empty string> <image-name>
    $ lein run <directory with poms> <group-id prefix to filter on OR use empty string> <image-name>

## Options


## Bugs

  * The dimensions of the resulting graph visualization make it hard to view a project with many dependencies. The graph is too wide.

### Remaining Work

* Add the storage functionality
* Add a few CLI options

## License

Copyright © 2014 Kenneth L. Graham

Distributed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html), same as Clojure.
