import React from 'react';
import {
  View,
  Text,
  StyleSheet,
} from 'react-native';
import { getAccuracyStatus } from '../utils/CompassUtils';

const LocationInfo = ({location, accuracy}) => {
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
});

export default LocationInfo;
