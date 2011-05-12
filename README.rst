============
OpenBciP300
============

This repository hosts the Open Brain Computer Interface software based on P300 paradigm, written in JAVA.

For many years people were grappling with the problem of communicating with computer equipment. Despite the steady increase of computing power and the opportunities that they offer, computer interfaces almost constantly are at a similar stage. The entire communication is usually based on pressing the appropriate buttons - from the keyboard to the virtual symbols on devices equipped with a touch screen. Brain-computer interface can be a great step forward in the development of communication between man and machine.

Created computer system and an EEG machine are a complete BCI (Brain-Computer Interface) system. Developed program is written in Java, and thus is a multiplatform application. It works in a multiple threads environment, and is divided into independent functional modules. Window graphics is realized with SWT graphics library developed by Eclipse Foundation.

The system is based on the P300 paradigm and highlighting the elements on the table that is displayed on a monitor in front of examined person. It allows the use of digital filters directly in the program, as well as communication with other application through the local network or the Internet.

Created system is an alternative to heavyweight systems such as BCI2000( www.bci2000.org ). It's 100% open source and free to use.

.. image:: https://github.com/bernii/openBciP300/raw/master/workinFilter.jpg
.. image:: https://github.com/bernii/openBciP300/raw/master/rSquare.jpg
.. image:: https://github.com/bernii/openBciP300/raw/master/classificationWeightsBased.jpg

About:
============

This was designed to operate as efficiently as possible based on the paradigm of P300 - based on the experience gathered by people who deal with issues of BCI on a daily basis. A series of consultations with the staff of the Department of Biomedical Physics, University of Warsaw was made to thoroughly understand the needs and improvements which may be introduced in this type of system. The program contains all the necessary options for effective operation of the BCI. Furthermore it allows network communication with other applications - both collecting signals from the program and sending to the program (for example to change an array of symbols displayed to the user).

Developed software is an alternative to complex and expensive software packages. The application was designed so that it was easy to use, and access to all the useful options was rapid. Changing the parameters of the program is done with a modern graphical interface. All modifications are automatically propagated, and their results are immediately visible to the user.

By creating communication with other applications via TCP / IP, you can use it to create a system of a remote cotrol. Communication takes place through a simple protocol based on XML.

Technicals:
============

The program was created in most using Java (version 1.6). RXTX driver was used to handle COM ports. The program has modular structure to allow easy upgrades and modifications. Communication between modules takes place through interfaces.

Java was chosen due to the fact that it is modern language, and its performance is entirely sufficient to establish a system for BCI, acting under a paradigm of P300 and acting in real time. Although the performance of Java is much worse than C or C + + (rankings available at http://shootout.alioth.debian.org), current JVM and performence of home PCs are entirely sufficient.

Created program works with devices that are compliant with OpenEEG - which are connected to a computer via serial COM or USB port. 


How to use:
============

* In bin directory there is jar package with working application. To start it use start.bat under Windows.

Prereqs:
============
* javolution for some real time optimized libs ( http://javolution.org/ )
* The Standard Widget Toolkit (comes with Eclipse) ( swt.jar )
* Apache log4j ( http://logging.apache.org/log4j )
* RXTX native serial an parallel port communication library ( http://logging.apache.org/log4j )
* all above included in libs subdirectory
