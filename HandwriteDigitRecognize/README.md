# HandwriteDigitRecognize
A toy app to recognize the hand-written digits by classfier, which is trained and evaluated on dataset MNIST.

## Environment:
- OpenCV4Android 2.4.10
- Android API 25

## Details:


1. Use the SVM in OpenCV to train a classfy model on the MNIST database. Load the model in Android to test on the hand-written digit;
2. Now the recognition on digit 8 and 9 is bad, I consider to save the false-recognition image to redo the SVM training in the future;


## Demo Show:


![Alt text](https://github.com/wblgers/OpenCV_Android_Plus/blob/master/picture/HandwriteDigitRecognize.gif)

## Blog Address:
[OpenCV机器学习：Android上利用SVM实现手写体数字识别](https://blog.csdn.net/wblgers1234/article/details/80241774)