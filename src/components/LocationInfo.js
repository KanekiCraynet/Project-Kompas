import React from 'react';
import {
  View,
  Text,
  StyleSheet,
} from 'react-native';
import { getAccuracyStatus } from '../utils/CompassUtils';

const LocationInfo = ({location, accuracy, compassAccuracy, isCalibrated}) => {
  const formatCoordinate = (coord) => {
    return coord.toFixed(6);
  };

  const accuracyStatus = getAccuracyStatus(accuracy);

  return (
    <View style={styles.container}>
      <View style={styles.locationCard}>
        <Text style={styles.title}>Informasi Lokasi</Text>
        
        <View style={styles.coordinateContainer}>
          <View style={styles.coordinateItem}>
            <Text style={styles.coordinateLabel}>Latitude:</Text>
            <Text style={styles.coordinateValue}>
              {formatCoordinate(location.latitude)}°
            </Text>
          </View>
          
          <View style={styles.coordinateItem}>
            <Text style={styles.coordinateLabel}>Longitude:</Text>
            <Text style={styles.coordinateValue}>
              {formatCoordinate(location.longitude)}°
            </Text>
          </View>
        </View>
        
        <View style={styles.accuracyContainer}>
          <Text style={styles.accuracyLabel}>Akurasi GPS:</Text>
          <View style={styles.accuracyInfo}>
            <Text style={[styles.accuracyValue, {color: accuracyStatus.color}]}>
              {accuracy.toFixed(1)}m
            </Text>
            <Text style={[styles.accuracyStatus, {color: accuracyStatus.color}]}>
              ({accuracyStatus.status})
            </Text>
          </View>
        </View>
        
        {/* Compass Accuracy */}
        {compassAccuracy > 0 && (
          <View style={styles.compassAccuracyContainer}>
            <Text style={styles.accuracyLabel}>Akurasi Kompas:</Text>
            <View style={styles.accuracyInfo}>
              <Text style={[styles.accuracyValue, {
                color: compassAccuracy > 0.8 ? '#4CAF50' : compassAccuracy > 0.5 ? '#FFC107' : '#FF5722'
              }]}>
                {Math.round(compassAccuracy * 100)}%
              </Text>
              <Text style={[styles.accuracyStatus, {
                color: compassAccuracy > 0.8 ? '#4CAF50' : compassAccuracy > 0.5 ? '#FFC107' : '#FF5722'
              }]}>
                ({compassAccuracy > 0.8 ? 'Sangat Baik' : compassAccuracy > 0.5 ? 'Baik' : 'Kurang Baik'})
              </Text>
            </View>
          </View>
        )}
        
        {/* Calibration Status */}
        <View style={styles.calibrationContainer}>
          <Text style={styles.accuracyLabel}>Status Kalibrasi:</Text>
          <View style={styles.accuracyInfo}>
            <View style={styles.calibrationStatus}>
              <View style={[
                styles.calibrationDot,
                { backgroundColor: isCalibrated ? '#4CAF50' : '#FFC107' }
              ]} />
              <Text style={[styles.accuracyStatus, {
                color: isCalibrated ? '#4CAF50' : '#FFC107'
              }]}>
                {isCalibrated ? 'Ter-kalibrasi' : 'Sedang Kalibrasi...'}
              </Text>
            </View>
          </View>
        </View>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    marginTop: 20,
    width: '100%',
  },
  locationCard: {
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 15,
    padding: 20,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.2)',
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#fff',
    textAlign: 'center',
    marginBottom: 15,
  },
  coordinateContainer: {
    marginBottom: 15,
  },
  coordinateItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  coordinateLabel: {
    fontSize: 14,
    color: '#ccc',
  },
  coordinateValue: {
    fontSize: 14,
    color: '#fff',
    fontWeight: 'bold',
  },
  accuracyContainer: {
    borderTopWidth: 1,
    borderTopColor: 'rgba(255, 255, 255, 0.2)',
    paddingTop: 15,
  },
  accuracyLabel: {
    fontSize: 14,
    color: '#ccc',
    marginBottom: 5,
  },
  accuracyInfo: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  accuracyValue: {
    fontSize: 16,
    fontWeight: 'bold',
  },
  accuracyStatus: {
    fontSize: 12,
    fontStyle: 'italic',
  },
  compassAccuracyContainer: {
    borderTopWidth: 1,
    borderTopColor: 'rgba(255, 255, 255, 0.2)',
    paddingTop: 15,
    marginTop: 10,
  },
  calibrationContainer: {
    borderTopWidth: 1,
    borderTopColor: 'rgba(255, 255, 255, 0.2)',
    paddingTop: 15,
    marginTop: 10,
  },
  calibrationStatus: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  calibrationDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginRight: 8,
  },
});

export default LocationInfo;
