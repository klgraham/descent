# descent

As projects grow in complexity, their dependencies often do as well. When release time comes, it may help to know how your various libraries depend on one another. Descent combines Clojure, Datomic, and Rhizome to track project dependencies over time.

Have yet to implement the Datomic storage. Will do that soon. For right now, the functionality is as follows:

  1. if you pass in a pom, you'll get a graph of all the dependencies. The graph will be written to a file.
  2. If you run again on a different pom, you'll get a diferent graph. You can then merge the two results to obtain a composite dependency graph.

## Installation

Download from http://example.com/FIXME.

## Usage

FIXME: explanation

    $ java -jar descent-0.1.0-standalone.jar [args]

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...

### Remaining Work


### That You Think
### Might be Useful

## License

Copyright © 2014 Kenneth L. Graham

Distributed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html), same as Clojure.
