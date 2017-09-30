# LiquidButton
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-LiquidButton-green.svg?style=true)](https://android-arsenal.com/details/1/3800)    

Customised UI from the idea of:https://dribbble.com/shots/2695600-Liquid-check
![alt tag](https://d13yacurqjgara.cloudfront.net/users/330174/screenshots/2695600/comp_2.gif)

## Example

![demo1](https://github.com/yoruriko/LiquidButton/blob/master/demo.gif)

In xml layout file
```java
      <com.gospelware.liquidbutton.LiquidButton
        android:id="@+id/button"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_centerInParent="true"
        android:clickable="true" />
```
Uses startPour() to start the animation.    
```java
      LiquidButton liquidButton = (LiquidButton) findViewById(R.id.button);
  
      liquidButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
            LiquidButton btn = (LiquidButton) v;
            btn.startPour();
      }
      });
```

Set fillAfter using setFillAfter(), default as not fill after
```java
      liquidButton.setFillAfter(true);
```
Set autoPlay using setAutoPlay(), default as not palying automatically
```java
      liquidButton.setAutoPlay(true);
```

  Register PourFinishListener to the widget, it will send a callback onPourFinish() when the animation completed, and onProgressUpdate() when new progress is updated;
```java
      liquidButton.setPourFinishListener(new LiquidButton.PourFinishListener() {
            @Override
            public void onPourFinish() {
                  Toast.makeText(MainActivity.this, "Finish", Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onProgressUpdate(float progress) {
                  textView.setText(String.format("%.2f", progress * 100) + "%");
            }          
      });
```

## How to update and finish the animation?

You'll able to uses changeProgress() to alternate the progress of the liquid level. (You need AutoPlay turn Off to be able to do that.) Progress are measure in float, where 1f = 100%;
```java
      liquidButton.changeProgress(progress);
```

By default the widget starts a finish animation when the progress is 1f, but you can also uses finishPour to start the finish Animation.
```java
      liquidButton.finishPour();
```

## How to?

**Gradle**        

```java
      dependencies {    
            compile 'com.gospelware.liquidbutton:liquidButtonLib:1.1.5'
      }
```

## To Do:

      Prgression update (finished)
      Alternating the Animation speed

