🌟 Project Summary: Dodge Game
Concept:
A small mobile game where:
✅ You control a running character (Running Back player)
✅ Avoid enemies (Linebackers) coming from the top
✅ You can “fling” a football to hit the enemies
✅ Motion is controlled using device tilt (Accelerometer & Magnetometer)
✅ Tap for faster movement
✅ Fling gesture to throw football
✅ 60 second countdown — shows "You Scored: X" at the end
✅ Background music + sound effects

🕹️ Gameplay
You tilt your phone — character moves left/right
Linebackers fall from the top — you dodge them
You can tap for faster movement
Fling to throw a football at the enemies
If enemies reach bottom — you score +1 if no collision, -1 if you collided
60 sec timer → shows score screen

🎮 Tech used
✅ Sensors:
SensorManager + Accelerometer + Magnetometer — for tilt control
✅ Custom View:
SurfaceView + Canvas drawing for the game scene
✅ Sound:
MediaPlayer (background music), SoundPool (collision sound)
✅ Gestures:
GestureDetector for tap & fling
✅ Game loop:
Own thread (Runnable), runs the game update & draw loop

🗺️ File structure
MainActivity.java → contains everything
GameSurface → inner class that implements the game
GestureListener → inner class for gestures
