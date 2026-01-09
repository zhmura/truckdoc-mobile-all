# TruckDoc Standout Module

## Overview

The TruckDoc Standout Module is a specialized Android library module that provides floating window and overlay functionality for the TruckDoc mobile application. This module enables the creation of floating UI elements that can appear above other applications, providing quick access to key features and information.

## Features

### Core Functionality
- **Floating Windows**: Create floating windows that overlay other applications
- **Quick Actions**: Provide quick access to frequently used features
- **Information Display**: Show important information in floating format
- **Interactive Elements**: Touch-enabled floating UI components
- **Multi-Window Support**: Support for multiple floating windows
- **Window Management**: Comprehensive window lifecycle management

### Advanced Features
- **Customizable Appearance**: Configurable window appearance and styling
- **Position Management**: Automatic and manual window positioning
- **Animation Support**: Smooth animations for window transitions
- **Touch Handling**: Advanced touch event handling and gesture recognition
- **Accessibility**: Accessibility support for floating windows
- **Performance Optimization**: Efficient rendering and memory management

## Architecture

### Technology Stack
- **Language**: Java
- **Window Management**: Android WindowManager API
- **UI Framework**: Custom floating window framework
- **Animation**: Android Animation framework
- **Touch Handling**: Custom touch event processing
- **Testing**: JUnit, Mockito, UI testing

### Key Components

#### Window Management
- **StandOutWindow**: Base class for floating windows
- **WindowManager**: Window lifecycle and state management
- **WindowController**: Window positioning and interaction control
- **WindowAnimator**: Window animation and transition management

#### UI Components
- **FloatingView**: Base floating view component
- **QuickActionView**: Quick action floating button
- **InfoDisplayView**: Information display floating window
- **ControlPanelView**: Floating control panel

#### Touch Handling
- **TouchManager**: Touch event processing and management
- **GestureDetector**: Gesture recognition and handling
- **DragHandler**: Window dragging and positioning
- **TouchListener**: Custom touch event listeners

#### Utilities
- **WindowUtils**: Window utility functions
- **AnimationUtils**: Animation utility functions
- **TouchUtils**: Touch handling utilities
- **DisplayUtils**: Display and positioning utilities

## Project Structure

```
src/main/java/wei/mark/standout/
├── window/                 # Window management
│   ├── StandOutWindow.java # Base window class
│   ├── WindowManager.java  # Window manager
│   ├── WindowController.java # Window controller
│   └── WindowAnimator.java # Window animator
├── view/                   # UI components
│   ├── FloatingView.java   # Base floating view
│   ├── QuickActionView.java # Quick action view
│   ├── InfoDisplayView.java # Info display view
│   └── ControlPanelView.java # Control panel view
├── touch/                  # Touch handling
│   ├── TouchManager.java   # Touch manager
│   ├── GestureDetector.java # Gesture detector
│   ├── DragHandler.java    # Drag handler
│   └── TouchListener.java  # Touch listener
├── animation/              # Animation
│   ├── WindowAnimator.java # Window animator
│   ├── FadeAnimator.java   # Fade animations
│   ├── SlideAnimator.java  # Slide animations
│   └── ScaleAnimator.java  # Scale animations
├── util/                   # Utilities
│   ├── WindowUtils.java    # Window utilities
│   ├── AnimationUtils.java # Animation utilities
│   ├── TouchUtils.java     # Touch utilities
│   └── DisplayUtils.java   # Display utilities
└── config/                 # Configuration
    ├── WindowConfig.java   # Window configuration
    └── AnimationConfig.java # Animation configuration
```

## Development Setup

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 17
- Android SDK API 33+
- Gradle 8.0+

### Build Configuration
The standout module supports multiple build variants:
- **Debug**: Development build with debugging enabled
- **Release**: Production build with ProGuard optimization

### Dependencies
Key dependencies include:
- **Android Support**: Support library components
- **JUnit & Mockito**: Testing frameworks
- **Custom Animation**: Internal animation framework

## Usage

### Basic Integration

#### 1. Add Dependency
```gradle
implementation project(':standout')
```

#### 2. Create Floating Window
```java
public class QuickActionWindow extends StandOutWindow {
    
    @Override
    public String getAppName() {
        return "Quick Actions";
    }
    
    @Override
    public int getAppIcon() {
        return R.drawable.ic_quick_action;
    }
    
    @Override
    public void createAndAttachView(int id, FrameLayout frame) {
        // Create and attach your floating view
        QuickActionView view = new QuickActionView(this);
        frame.addView(view);
    }
    
    @Override
    public StandOutLayoutParams getParams(int id, Window window) {
        return new StandOutLayoutParams(
            id,
            Window.Width.WRAP_CONTENT,
            Window.Height.WRAP_CONTENT,
            StandOutLayoutParams.CENTER,
            StandOutLayoutParams.CENTER
        );
    }
}
```

#### 3. Show Floating Window
```java
// Show the floating window
StandOutWindow.show(this, QuickActionWindow.class, StandOutWindow.DEFAULT_ID);

// Hide the floating window
StandOutWindow.close(this, QuickActionWindow.class, StandOutWindow.DEFAULT_ID);
```

### Custom Floating Views

#### 1. Create Custom View
```java
public class CustomFloatingView extends FloatingView {
    
    public CustomFloatingView(Context context) {
        super(context);
        init();
    }
    
    private void init() {
        // Initialize your custom floating view
        setBackgroundResource(R.drawable.floating_background);
        
        // Add your UI components
        Button actionButton = new Button(getContext());
        actionButton.setText("Action");
        actionButton.setOnClickListener(v -> {
            // Handle button click
        });
        
        addView(actionButton);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Handle touch events
        return super.onTouchEvent(event);
    }
}
```

#### 2. Use Custom View in Window
```java
@Override
public void createAndAttachView(int id, FrameLayout frame) {
    CustomFloatingView view = new CustomFloatingView(this);
    frame.addView(view);
}
```

### Window Positioning

#### 1. Fixed Position
```java
@Override
public StandOutLayoutParams getParams(int id, Window window) {
    return new StandOutLayoutParams(
        id,
        Window.Width.WRAP_CONTENT,
        Window.Height.WRAP_CONTENT,
        StandOutLayoutParams.CENTER,
        StandOutLayoutParams.CENTER
    );
}
```

#### 2. Draggable Position
```java
@Override
public StandOutLayoutParams getParams(int id, Window window) {
    StandOutLayoutParams params = new StandOutLayoutParams(
        id,
        Window.Width.WRAP_CONTENT,
        Window.Height.WRAP_CONTENT,
        StandOutLayoutParams.CENTER,
        StandOutLayoutParams.CENTER
    );
    
    params.setFlags(StandOutLayoutParams.FLAG_NOT_FOCUSABLE |
                   StandOutLayoutParams.FLAG_NOT_TOUCH_MODAL |
                   StandOutLayoutParams.FLAG_LAYOUT_IN_SCREEN);
    
    return params;
}
```

### Window Animations

#### 1. Fade Animation
```java
@Override
public void onShow(int id) {
    super.onShow(id);
    
    // Apply fade-in animation
    AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
    fadeIn.setDuration(300);
    getView().startAnimation(fadeIn);
}
```

#### 2. Slide Animation
```java
@Override
public void onHide(int id) {
    super.onHide(id);
    
    // Apply slide-out animation
    TranslateAnimation slideOut = new TranslateAnimation(
        Animation.RELATIVE_TO_SELF, 0.0f,
        Animation.RELATIVE_TO_SELF, 1.0f,
        Animation.RELATIVE_TO_SELF, 0.0f,
        Animation.RELATIVE_TO_SELF, 0.0f
    );
    slideOut.setDuration(300);
    getView().startAnimation(slideOut);
}
```

## API Reference

### StandOutWindow
Base class for floating windows.

#### Methods
- `getAppName()`: Get application name
- `getAppIcon()`: Get application icon
- `createAndAttachView(id, frame)`: Create and attach view
- `getParams(id, window)`: Get window parameters
- `onShow(id)`: Called when window is shown
- `onHide(id)`: Called when window is hidden

### StandOutLayoutParams
Window layout parameters.

#### Properties
- `width`: Window width
- `height`: Window height
- `x`: X position
- `y`: Y position
- `flags`: Window flags

### FloatingView
Base floating view component.

#### Methods
- `onTouchEvent(event)`: Handle touch events
- `onDragEvent(event)`: Handle drag events
- `setDraggable(draggable)`: Set draggable state
- `setResizable(resizable)`: Set resizable state

## Configuration

### Window Configuration
```java
WindowConfig config = new WindowConfig.Builder()
    .setDraggable(true)
    .setResizable(false)
    .setFocusable(false)
    .setTouchModal(false)
    .build();
```

### Animation Configuration
```java
AnimationConfig config = new AnimationConfig.Builder()
    .setShowAnimation(AnimationType.FADE)
    .setHideAnimation(AnimationType.SLIDE)
    .setDuration(300)
    .build();
```

### Touch Configuration
```java
TouchConfig config = new TouchConfig.Builder()
    .setTouchEnabled(true)
    .setDragEnabled(true)
    .setGestureEnabled(true)
    .build();
```

## Testing

### Unit Tests
- **Location**: `src/test/java/`
- **Coverage**: Business logic and window management
- **Frameworks**: JUnit 4, Mockito

### UI Tests
- **Location**: `src/androidTest/java/`
- **Coverage**: UI components and interactions
- **Frameworks**: Espresso, AndroidJUnit4

### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test variant
./gradlew testDebugUnitTest

# Run UI tests
./gradlew connectedAndroidTest
```

## Build Process

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### AAR Generation
```bash
# Generate debug AAR
./gradlew assembleDebug

# Generate release AAR
./gradlew assembleRelease
```

## Performance Optimization

### Window Optimization
- **Efficient Rendering**: Optimized rendering for floating windows
- **Memory Management**: Proper memory management for window objects
- **Animation Optimization**: Efficient animation handling
- **Touch Optimization**: Optimized touch event processing

### Memory Management
- **Object Pooling**: Reuse window objects
- **View Recycling**: Recycle view components
- **Garbage Collection**: Proper cleanup and garbage collection

### Performance Monitoring
- **Window Metrics**: Track window performance
- **Animation Metrics**: Monitor animation performance
- **Touch Metrics**: Track touch event performance

## Security Considerations

### Permissions
The standout module requires the following permissions:
- `SYSTEM_ALERT_WINDOW`: For displaying floating windows
- `ACCESSIBILITY_SERVICE`: For accessibility support (optional)

### Security Best Practices
- **Input Validation**: Validate all user inputs
- **Window Isolation**: Isolate floating windows from other applications
- **Touch Security**: Secure touch event handling
- **Memory Security**: Secure memory management

## Troubleshooting

### Common Issues
1. **Window Not Showing**: Check SYSTEM_ALERT_WINDOW permission
2. **Touch Not Working**: Verify touch event handling
3. **Animation Issues**: Check animation configuration
4. **Performance Issues**: Monitor window performance metrics

### Debugging
- Enable window logging in `StandOutWindow.java`
- Check window parameters and configuration
- Monitor touch event processing
- Verify animation settings

## Contributing

### Code Style
- Follow Java coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Maintain consistent formatting

### Git Workflow
1. Create feature branch from `main`
2. Implement changes with tests
3. Run full test suite
4. Submit pull request with description

### Code Review
- All changes require code review
- Ensure tests pass
- Verify build success
- Check for performance implications

## License

This module is part of the TruckDoc mobile application and is proprietary software. 