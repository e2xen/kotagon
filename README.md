# kotagon

Kotagon is an Information Flow Control Language for Kotlin programming language.

A primary design goal was to enhance usability compared to existing IFC tools. Kotagon significantly reduces the 
need for extensive code labeling by encapsulating sensitive data within a _Labeled_ monadic container. This design 
minimizes the syntactic noise often associated with IFC annotations and does not require developers to label method 
signatures or exception paths, leading to cleaner and more maintainable code.

## Build

To build this tool locally, the JDK 21 is required to be pre-installed.

Run the following command in the terminal:
```shell
./gradlew build
```

Then, the library JAR-file will be available in the directory: `./build/libs/`.

## Kotagon key components

#### Policy

Kotagon policies are developed with inspiration from Paragon policies. Policies can be classified as either static 
or dynamic. The key feature of static policies is that they do not change over time. In contrast, dynamic policies 
include conditions that specify the situations in which the information may flow.

#### Labeled

Kotagon enables the specification of intended information flows within a program, while the runtime library 
ensures that these intended flows remain unviolated. This is accomplished by labeling data with policies. 
Data containers (such as objects, files, and other types) are categorized according to the information they store. 
Each container is labeled to reflect the sensitivity of its contents.

#### Locks

To enable conditional information flows based on the system state, a policy-level representation of that state is 
necessary. This is achieved through a new object type called a lock. It is a typed predicate that represents the 
policy-relevant condition of the system. A lock functions as a specialized boolean value that developers utilize 
to indicate security-related events within the system.


## Features

* Support of static policies
* Support of dynamic policies (unary and binary locks)
* Extensible lock mechanism
* Less code labelling compared compared to Paragon
* Minimal configuration

## Limitations

* The information leak is possible if the library is used incorrectly 
* Interoperability with standard Java and Kotlin libraries remains a challenge
* Lock evaluation requires developers to explicitly provide lock arguments
* We do not yet support all the features available in the Paragon language

## Contribution
You can contribute to our project through pull requests - we are glad to new ideas and fixes.

## Credits

This project is developed by:
* [@mcflydesigner](https://github.com/mcflydesigner)
* [@e2xen](https://github.com/e2xen)

## License
The project is released and distributed under [MIT License](https://en.wikipedia.org/wiki/MIT_License).
