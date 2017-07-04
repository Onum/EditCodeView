# EditCodeView

[![Bintray](https://img.shields.io/bintray/v/onum/maven/editcodeview.svg?maxAge=2592000)](https://bintray.com/onum/maven/editcodeview) [![License:
MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

![](https://github.com/Onum/EditCodeView/blob/assets/assets/gif-animations/animation_1.gif?raw=true)

![](https://github.com/Onum/EditCodeView/blob/assets/assets/gif-animations/animation_2.gif?raw=true)

![](https://github.com/Onum/EditCodeView/blob/assets/assets/gif-animations/animation_3.gif?raw=true)


# Customized attributes

Attrs | Type
------------ | -------------
textSize | dimension
textColor | color
codeLength | integer
codeHiddenMode | boolean
codeHiddenMask | string
underlineStroke | dimension
underlineReductionScale | float
underlineFilledColor | color
underlineSelectedColor | color
underlineBaseColor | color
underlineCursorColor | color
underlineCursorEnabled | boolean
fontStyle | enum

# How to get code
``` java
EditCodeView editCodeView = (EditCodeView) findViewById(R.id.edit_code);
        editCodeView.setEditCodeListener(new EditCodeListener() {
            @Override
            public void onCodeReady(String code) {
                // ...
            }
        });
```

#### Tip
Use the paddingBottom that would raise the underline to the text

# License

The library is distributed under the MIT [LICENSE](https://opensource.org/licenses/MIT).

