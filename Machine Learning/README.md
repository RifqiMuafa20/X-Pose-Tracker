# X Pose Tracker - Machine Learning

A machine learning project for human activity recognition using body joint angle features and BiLSTM neural networks.

## Overview

This project develops a BiLSTM-based classification model to recognize human exercise activities based on body joint angle measurements. The model analyzes skeletal angles to classify different workout movements including push-ups, pull-ups, sit-ups, and lunges.

## Dataset

The dataset contains body joint angles collected from 8 subjects performing various exercises:
- **Angles**: 3D body joint angles
- **Angles2D**: 2D body joint angle projections
- **Angles3D**: 3D angle variations
- **Torso Angles**: Additional torso-specific angle measurements

## Model Architecture

- **Model Type**: BiLSTM (Bidirectional Long Short-Term Memory)
- **Input Features**: Body joint angles (2D, 3D, and torso angles)
- **Output**: Exercise activity classification
- **Evaluation Method**: Leave-One-Subject-Out (LOSO) Cross-Validation

## Model Performance

The BiLSTM model demonstrates excellent performance across all evaluation scenarios:

| Metric | Performance |
|--------|-------------|
| Mean Accuracy (All Scenarios) | > 97% |
| Mean Macro F1-Score (All Scenarios) | > 0.96 |
| **Best Accuracy** | **99.81%** |
| **Best Macro F1-Score** | **0.998** |

**Best Configuration**:
- Dataset: 2D body joint angles + torso angles
- Batch Size: 8
- Learning Rate: 0.0001
- BiLSTM Layers: 1

The stable LOSO cross-validation results across all scenarios demonstrate that the model generalizes well to unseen subjects, making it suitable for real-world deployment.

## File Structure

- `HAR_using_BiLSTM.ipynb`: Main model training and evaluation notebook
- `Angles/`, `Angles2D/`, `Angles3D/`: Body angle datasets
- `Final_Model/`: Trained models and preprocessing parameters
- `Result/`: Evaluation results for different scenarios
- `Tuner_Keras/`: Hyperparameter tuning results

## Requirements

See `requirements.txt` for dependencies.

## Usage

1. Activate the virtual environment:
   ```bash
   mp-env\Scripts\Activate.ps1
   ```

2. Run the main training notebook:
   ```bash
   jupyter notebook HAR_using_BiLSTM.ipynb
   ```

## Models

- `final_model.keras`: Full trained model
- `Bilstm_model.tflite`: TensorFlow Lite version for mobile/edge deployment
- `zscore_mean.npy`, `zscore_std.npy`: Normalization parameters
