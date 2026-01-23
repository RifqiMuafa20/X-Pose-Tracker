# X Pose Tracker

A comprehensive Human Activity Recognition (HAR) system that uses pose estimation and BiLSTM model to classify physical fitness tests. This project consists of a machine learning project for training and model optimization, and an Android application for real-time exercise classification and repetition counting. 

## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [Machine Learning Component](#machine-learning-component)
- [Android Application](#android-application)
- [Contributing](#contributing)
- [License](#license)

## Project Overview

X Pose Tracker is a mobile application that uses MediaPipe pose estimation combined with a Bidirectional LSTM (BiLSTM) neural network to classify physical fitness tests in real-time. The system extracts skeletal landmarks from video frames, calculates body joint angles, and uses temporal sequence analysis to identify the performed exercise. A rule-based repetition counting system was designed using joint angle thresholds and posture validation.

![X-Pose Tracker App Banner](<Banner Aplikasi.png>)

### Key Technologies

- **MediaPipe**: For pose estimation and landmark detection
- **TensorFlow/Keras**: For deep learning model development
- **BiLSTM**: Bidirectional LSTM for temporal sequence analysis
- **Android Kotlin**: For mobile application development
- **TensorFlow Lite**: For efficient on-device inference

## Features

### Machine Learning

- ✅ Single-person pose estimation using MediaPipe
- ✅ 2D angle calculations
- ✅ Leave-One-Subject-Out (LOSO) cross-validation
- ✅ Hyperparameter tuning with Keras Tuner (Bayesian Optimization)
- ✅ BiLSTM-based temporal sequence modeling (30 frame per sequence)
- ✅ Model optimization and conversion to TensorFlow Lite format

### Android Application

- ✅ Real-time pose estimation using Android cameraX
- ✅ Exercise classification with majority voting
- ✅ Multiple exercise types support (Push-Up, Lunges, Sit-Up, Pull-Up)
- ✅ Efficient on-device inference using TensorFlow Lite
- ✅ User-friendly interface
- ✅ Pose visualization with skeleton overlays
- ✅ A rule-based repetition counting and posture validation system  

## Machine Learning Component

### Setup Instructions

#### 1. Create Virtual Environment

```bash
cd "Machine Learning"
python -m venv mp-env

# On Windows
mp-env\Scripts\activate

# On Linux/Mac
source mp-env/bin/activate
```

#### 2. Install Dependencies


```bash
pip install -r requirements.txt
```

### Training

Run the training notebook:

```bash
jupyter notebook HAR_using_BiLSTM.ipynb
```

Results can be found in the `Result/`  directories.

## Android Application

### Installation

#### Prerequisites

1. Android Studio Bumblebee or later
2. Android SDK (API 24 minimum)
3. Kotlin 1.8 or later

#### Build Instructions

1. Clone or download the project:
```bash
cd "X Pose Tracker Android App"
```

2. Open in Android Studio:
```
File → Open → Select the X Pose Tracker Android App folder
```

3. Sync Gradle files:
```
File → Sync Now
```

4. Connect an Android device or start an emulator

5. Run the application:
```
Run → Run 'app'
```

Or using Gradle:
```bash
./gradlew assembleDebug
./gradlew installDebug
```

#### Build Configuration

- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 34 (Android 14)
- **Compile SDK**: API 35
- **Language**: Kotlin
- **JVM Target**: Java 11

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/improvement`)
3. Commit your changes (`git commit -m 'Add improvement'`)
4. Push to the branch (`git push origin feature/improvement`)
5. Open a Pull Request

## License

This project is provided as-is for educational and research purposes.

---

**Project Authors**: Muhammad Rifqi Muafa

**Last Updated**: January 2026

**Version**: 1.0

---

## Citation

If you use this project in your research, please cite:

```
@software{xposetracker2026,
  title={X Pose Tracker: Human Activity Recognition Using BiLSTM},
  author={[Muhammad Rifqi Muafa]},
  year={2026},
  url={https://github.com/yourusername/X-Pose-Tracker}
}
```

## Support

For issues, questions, or suggestions:
- Create an issue on GitHub
- Contact the development team
- Check existing documentation and FAQs

---

**Disclaimer**: This application is designed for fitness tracking purposes. Always consult with healthcare professionals before starting any new exercise program.
