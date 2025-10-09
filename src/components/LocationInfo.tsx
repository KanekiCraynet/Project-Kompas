import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

interface LocationData {
  latitude: number;
  longitude: number;
  accuracy: number;
  altitude?: number;
  speed?: number;
}

interface LocationInfoProps {
  location: LocationData;
  heading: number;
  direction: string;
}

const LocationInfo: React.FC<LocationInfoProps> = ({ location, heading, direction }) => {
  const formatCoordinate = (coord: number, isLatitude: boolean): string => {
    const abs = Math.abs(coord);
    const degrees = Math.floor(abs);
    const minutes = Math.floor((abs - degrees) * 60);
    const seconds = ((abs - degrees) * 60 - minutes) * 60;
    const direction = isLatitude 
      ? (coord >= 0 ? 'N' : 'S') 
      : (coord >= 0 ? 'E' : 'W');
    
    return `${degrees}°${minutes}'${seconds.toFixed(1)}"${direction}`;
  };

  const formatSpeed = (speed?: number): string => {
    if (!speed || speed < 0) return '0.0 km/h';
    return `${(speed * 3.6).toFixed(1)} km/h`;
  };

  const formatAltitude = (altitude?: number): string => {
    if (!altitude) return 'N/A';
    return `${altitude.toFixed(1)} m`;
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Informasi Lokasi</Text>
      </View>
      
      <View style={styles.infoGrid}>
        {/* Coordinates */}
        <View style={styles.infoRow}>
          <Text style={styles.label}>Latitude:</Text>
          <Text style={styles.value}>{formatCoordinate(location.latitude, true)}</Text>
        </View>
        
        <View style={styles.infoRow}>
          <Text style={styles.label}>Longitude:</Text>
          <Text style={styles.value}>{formatCoordinate(location.longitude, false)}</Text>
        </View>
        
        {/* Heading and Direction */}
        <View style={styles.infoRow}>
          <Text style={styles.label}>Heading:</Text>
          <Text style={styles.value}>{Math.round(heading)}° {direction}</Text>
        </View>
        
        {/* Accuracy */}
        <View style={styles.infoRow}>
          <Text style={styles.label}>Akurasi GPS:</Text>
          <Text style={styles.value}>{location.accuracy.toFixed(1)} m</Text>
        </View>
        
        {/* Altitude */}
        <View style={styles.infoRow}>
          <Text style={styles.label}>Ketinggian:</Text>
          <Text style={styles.value}>{formatAltitude(location.altitude)}</Text>
        </View>
        
        {/* Speed */}
        <View style={styles.infoRow}>
          <Text style={styles.label}>Kecepatan:</Text>
          <Text style={styles.value}>{formatSpeed(location.speed)}</Text>
        </View>
      </View>
      
      {/* Status indicator */}
      <View style={styles.statusContainer}>
        <View style={[styles.statusDot, { backgroundColor: '#4ecdc4' }]} />
        <Text style={styles.statusText}>GPS Aktif</Text>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    bottom: 100,
    left: 20,
    right: 20,
    backgroundColor: 'rgba(26, 26, 46, 0.9)',
    borderRadius: 15,
    padding: 20,
    borderWidth: 1,
    borderColor: '#4ecdc4',
  },
  header: {
    marginBottom: 15,
    alignItems: 'center',
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#4ecdc4',
  },
  infoGrid: {
    marginBottom: 15,
  },
  infoRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
    paddingVertical: 2,
  },
  label: {
    fontSize: 14,
    color: '#ccc',
    flex: 1,
  },
  value: {
    fontSize: 14,
    color: '#fff',
    fontWeight: '600',
    textAlign: 'right',
    flex: 1,
  },
  statusContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingTop: 10,
    borderTopWidth: 1,
    borderTopColor: '#333',
  },
  statusDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginRight: 8,
  },
  statusText: {
    fontSize: 12,
    color: '#4ecdc4',
    fontWeight: '600',
  },
});

export default LocationInfo;
