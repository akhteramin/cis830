# cis830
1. How to run the code using openMP annotation?

Ans: Please go to the src directory of the project in the terminal. Then run this command: 

java -jar keysearch/omp4j-1.2.jar keysearch/MultiThreadMakeKey.java

This will generate the classfile and then just run the class execution command:

java keysearch/MultiThreadMakeKey {String you want to encrypt} {Number of bytes you want to hide. Remember this number should be divisible by 8 i.e. 8, 16, 24 etc}

As example: java keysearch/MultiThreadMakeKey rit@gmail.com 8