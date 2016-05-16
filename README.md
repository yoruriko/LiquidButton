# LiquidButton

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

Set fillAfter using setFillAfter(), befault as not fill after
```java
liquidCheckView.setFillAfter(true);
```

  Register PourFinishListener to the widget, it will send a callback onPourFinish() when the animation completed
```java
  liquidButton.setPourListener(new LiquidButton.PourFinishListener() {
    @Override
    public void onPourFinish() {
    Toast.makeText(MainActivity.this, "Loading Finish!", Toast.LENGTH_SHORT).show();
    }
  });
```
## How to?

**Gradle**        

```java
dependencies {    
  compile 'com.gospelware.liquidbutton:liquidButtonLib:1.1.1'    
}
```

