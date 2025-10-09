import React, { useState, useEffect } from 'react';
import {
  StyleSheet,
  View,
  Text,
  Dimensions,
  StatusBar,
  Alert,
  PermissionsAndroid,
  Platform,
} from 'react-native';
import { magnetometer, setUpdateIntervalForType, SensorTypes } from 'react-native-sensors';
import Geolocation from 'react-native-geolocation-service';
import { request, PERMISSIONS, RESULTS } from 'react-native-permissions';
import CompassNeedle from './src/components/CompassNeedle';
import LocationInfo from './src/components/LocationInfo';
import CompassBackground from './src/components/CompassBackground';

const { width, height } = Dimensions.get('window');

interface CompassData {
  x: number;
  y: number;
  z: number;
}

interface LocationData {
  latitude: number;
  longitude: number;
  accuracy: number;
  altitude?: number;
  speed?: number;
}

const App: React.FC = () => {
  const [compassData, setCompassData] = useState<CompassData>({ x: 0, y: 0, z: 0 });
  const [location, setLocation] = useState<LocationData | null>(null);
  const [heading, setHeading] = useState<number>(0);
  const [isLocationEnabled, setIsLocationEnabled] = useState<boolean>(false);
  const [magneticDeclination, setMagneticDeclination] = useState<number>(0);

  // Set update interval untuk sensor
  setUpdateIntervalForType(SensorTypes.magnetometer, 100);

  useEffect(() => {
    requestPermissions();
    startCompass();
    startLocationTracking();
    
    return () => {
      // Cleanup subscriptions
    };
  }, []);

  const requestPermissions = async () => {
    try {
      // Request location permission
      const locationPermission = await request(
        Platform.OS === 'ios' 
          ? PERMISSIONS.IOS.LOCATION_WHEN_IN_USE 
          : PERMISSIONS.ANDROID.ACCESS_FINE_LOCATION
      );

      if (locationPermission === RESULTS.GRANTED) {
        setIsLocationEnabled(true);
      } else {
        Alert.alert(
          'Permission Required',
          'Location permission is required for accurate compass readings'
        );
      }
    } catch (error) {
      console.error('Permission request error:', error);
    }
  };

  const startCompass = () => {
    const subscription = magnetometer.subscribe(({ x, y, z }: CompassData) => {
      setCompassData({ x, y, z });
      
      // Calculate heading from magnetometer data
      let angle = Math.atan2(y, x) * (180 / Math.PI);
      
      // Adjust for magnetic declination if location is available
      if (location) {
        angle += magneticDeclination;
      }
      
      // Normalize angle to 0-360 degrees
      angle = (angle + 360) % 360;
      setHeading(angle);
    });

    return subscription;
  };

  const startLocationTracking = () => {
    if (!isLocationEnabled) return;

    Geolocation.getCurrentPosition(
      (position) => {
        const { latitude, longitude, accuracy, altitude, speed } = position.coords;
        setLocation({
          latitude,
          longitude,
          accuracy,
          altitude,
          speed,
        });
        
        // Calculate magnetic declination based on location
        calculateMagneticDeclination(latitude, longitude);
      },
      (error) => {
        console.error('Location error:', error);
        Alert.alert('Location Error', 'Unable to get current location');
      },
      {
        enableHighAccuracy: true,
        timeout: 15000,
        maximumAge: 10000,
      }
    );

    // Watch position for continuous updates
    const watchId = Geolocation.watchPosition(
      (position) => {
        const { latitude, longitude, accuracy, altitude, speed } = position.coords;
        setLocation({
          latitude,
          longitude,
          accuracy,
          altitude,
          speed,
        });
      },
      (error) => {
        console.error('Location watch error:', error);
      },
      {
        enableHighAccuracy: true,
        distanceFilter: 10, // Update every 10 meters
      }
    );

    return watchId;
  };

  const calculateMagneticDeclination = (lat: number, lon: number) => {
    // Simplified magnetic declination calculation
    // In a real app, you might want to use a more accurate service
    const declination = Math.sin((lat * Math.PI) / 180) * 
                       Math.cos((lon * Math.PI) / 180) * 0.1;
    setMagneticDeclination(declination);
  };

  const getDirectionName = (heading: number): string => {
    const directions = [
      'N', 'NNE', 'NE', 'ENE', 'E', 'ESE', 'SE', 'SSE',
      'S', 'SSW', 'SW', 'WSW', 'W', 'WNW', 'NW', 'NNW'
    ];
    const index = Math.round(heading / 22.5) % 16;
    return directions[index];
  };

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1a1a2e" />
      
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.title}>Kompas Digital</Text>
        <Text style={styles.subtitle}>
          {Math.round(heading)}° {getDirectionName(heading)}
        </Text>
      </View>

      {/* Compass Container */}
      <View style={styles.compassContainer}>
        <CompassBackground size={Math.min(width, height) * 0.7} />
        <CompassNeedle 
          heading={heading} 
          size={Math.min(width, height) * 0.7}
        />
      </View>

      {/* Location Info */}
      {location && (
        <LocationInfo 
          location={location}
          heading={heading}
          direction={getDirectionName(heading)}
        />
      )}

      {/* Sensor Data Debug (optional) */}
      <View style={styles.debugContainer}>
        <Text style={styles.debugText}>
          X: {compassData.x.toFixed(2)} | Y: {compassData.y.toFixed(2)} | Z: {compassData.z.toFixed(2)}
        </Text>
        {magneticDeclination !== 0 && (
          <Text style={styles.debugText}>
            Magnetic Declination: {magneticDeclination.toFixed(2)}°
          </Text>
        )}
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a2e',
    alignItems: 'center',
    justifyContent: 'center',
  },
  header: {
    position: 'absolute',
    top: 60,
    alignItems: 'center',
    zIndex: 10,
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#fff',
    marginBottom: 5,
  },
  subtitle: {
    fontSize: 18,
    color: '#4ecdc4',
    fontWeight: '600',
  },
  compassContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    position: 'relative',
  },
  debugContainer: {
    position: 'absolute',
    bottom: 30,
    alignItems: 'center',
  },
  debugText: {
    color: '#666',
    fontSize: 12,
    marginBottom: 2,
  },
});

export default App;
