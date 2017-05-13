# Clone SDK & Sample Code

## What is Clone?

Clone (https://www.autonomous.ai/clone-telepresence-smart-office-robot) is an Android-based robot designed by Autonomous.  It is a 4-foot tall robot, capable of moving around, running Android apps, and being controlled by a smartphone.

## Develop Apps for Clone

Clone is based on Android, so building a Clone app is the same with building an Android app.  

The Clone SDK lets developers:

* instruct Clone to move at a given speed, direction, and amount of time
* read Clone's internal sensors including velocity, battery level, bumper, and IMU
* see and hear what Clone sees & hears


![ScreenShot](https://github.com/duyhtq/HelloClone/blob/master/diagram.jpg)

## Build your first Clone app

Import **HelloClone** project into Android Studio.

```
/HelloClone/RobotApp/
/HelloClone/PhoneApp/
```

Update Config.java with the your ROBOT ID.

```
public static String ROBOT_ID ="YOUR_ROBOT_ID";
```

Install the **RobotApp** on the Android tablet on Clone.  

Install **PhoneApp** on your Android phone.

That's it.  You now can use your phone to remotely control Clone to move around, capture photos, or record videos.

## Clone SDK

