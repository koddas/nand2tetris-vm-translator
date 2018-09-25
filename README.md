# nand2tetris-vm-translator
This small piece of software translates static pseudocode as described in chapters 7 and 8 of *The Elements of Computing System* to VM code runnable on the book's VM emulator.

## Features

The program converts *push/pop* commands as follows:

* **push *c*** -> **push constant *c***, where *c* is a constant,
* **push *x*** -> **push static *n***, where *x* is a variable name and n is a memory address,
* **pop *n*** -> **pop that *n***, where n is a memory address, and
* **pop *x*** -> **pop static *n***, where *x* is a variable name and n is a memory address.

It also supports a *print* macro, that "prints" an 8-bit integer by writing it to the first word in the graphics memory. It translates as follows:

* **print** -> **pop that 16384**, writes the last element on the stack to the graphics memory
* **print *c*** -> **push constant *c*, pop that 16384**, where *c* is a constant, and
* **print *x*** -> **push static *n*, pop that 16384**, where *x* is a variable name and n is a memory address.

## Contents

The project consists of a single file of Java sourcecode, accompanied by three example files:

* [loop.py](loop.py), a piece of Python code describing a simple loop.
* [loop-pseudo.vm](loop-pseudo.vm), a file containing pseudo VM code. Please note that most comments will be lost in translation.
* [loop-sane.vm](loop-sane.vm), a file showcasing what the translated code from *loop-pseudo.vm* will look like. Please note that the file has been manually edited with comments to match the comments from *loop-pseudo.vm*.

## How to build

Make sure that you have installed a Java 8 SDK before attempting to install the software. The software makes use of features not available prior to Java 8. Once done, compile the software by running


```bash
$ javac Translator.java
```

The software is now compiled and ready to be used.

## Usage

The software runs from the terminal. To translate a file, simply type

```bash
$ java Translator sourcefile.vm result.vm
```

where **sourcefile.vm** contains pseudocode and **result.vm** contains the resulting VM code.
