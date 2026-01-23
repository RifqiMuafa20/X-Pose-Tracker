# X Pose Tracker Android App

A mobile application for classify and counting repetition for physical fitness tests using human pose estimation and BiLSTM model.

## Features

- **Pose Detection**: Real-time pose estimation using TensorFlow Lite ML models
- **Camera Integration**: Direct camera access for live pose tracking
- **Member Management**: Create and manage member profiles with details and history
- **Event Tracking**: Record and organize pose tracking events and activities
- **Result Analysis**: View detailed analysis and results of pose tracking sessions
- **Responsive UI**: User-friendly interface with intuitive navigation

## Tech Stack

- **Language**: Kotlin
- **Minimum SDK**: 24
- **Target SDK**: 34
- **Machine Learning**: TensorFlow Lite
- **Database**: Room
- **Architecture**: MVVM with Data Binding
- **UI Components**: AndroidX, Navigation, Fragment

## Key Libraries

- **TensorFlow Lite**: ML model inference
- **CameraX**: Modern camera library for pose detection
- **Room**: Local database for data persistence
- **Glide**: Image loading and caching
- **MPAndroidChart**: Data visualization
- **Kotlin Coroutines**: Asynchronous programming

## Project Structure

```
app/
├── src/main/
│   ├── java/com/rifqidev/x_posetracker/
│   │   ├── ui/              # UI activities and fragments
│   │   ├── data/            # Database and data models
│   │   ├── repository/      # Data repositories
│   │   ├── adapter/         # RecyclerView adapters
│   │   ├── customview/      # Custom UI components
│   │   └── utils/           # Utility functions
│   ├── res/                 # Resources (layouts, strings, drawables)
│   └── ml/                  # ML model files
└── schemas/                 # Room database schemas
```

## Permissions

- **Camera**: For pose detection and real-time tracking
- **Read Media Images**: Access device images for processing
- **Internet**: For remote operations (if needed)

## License

All rights reserved.

## Contact

For questions or support, please contact the development team.
