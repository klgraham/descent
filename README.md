# Descent

As projects grow in complexity, their dependencies often do as well. When release time comes, it may help to know how your various libraries depend on one another. Descent lets you visualize project dependencies.

## Installation

  1. Download from https://github.com/klgraham/descent.
  2. `cd` to the descent directory.
  3. Run `lein uberjar` if you want to run using a jar, then look at the Usage below.
  4. Run `lein run <args>`, if you don't want to use a jar. See Usage example for arguments.

## Usage

    $ java -jar descent-0.1.0-standalone.jar <directory with poms> <group-id prefix to filter on OR use empty string> <image-name>
    $ lein run <directory with poms> <group-id prefix to filter on OR use empty string> <image-name>

## License

Copyright © 2014 Kenneth L. Graham

Distributed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html), same as Clojure.
