import React, {useState, useEffect, useRef} from 'react';
import {
  SafeAreaView,
  StyleSheet,
  StatusBar,
  View,
  Text,
  Alert,
  Dimensions,
} from 'react-native';
import {PermissionsAndroid, Platform} from 'react-native';
import Geolocation from 'react-native-geolocation-service';
import {magnetometer, setUpdateIntervalForType, SensorTypes} from 'react-native-sensors';
import CompassComponent from './components/CompassComponent';
import LocationInfo from './components/LocationInfo';
import Header from './components/Header';
import {
  calculateHeading,
  smoothHeading,
  getDetailedDirection,
  validateMagnetometerData,
  getAccuracyStatus
} from './utils/CompassUtils';

const {width, height} = Dimensions.get('window');

const App = () => {
  const [magnetometerData, setMagnetometerData] = useState({x: 0, y: 0, z: 0});
  const [heading, setHeading] = useState(0);
  const [smoothedHeading, setSmoothedHeading] = useState(0);
  const [location, setLocation] = useState(null);
  const [accuracy, setAccuracy] = useState(0);
  const [isLocationEnabled, setIsLocationEnabled] = useState(false);
  const previousHeadingRef = useRef(0);

  useEffect(() => {
    requestPermissions();
    setupMagnetometer();
    setupLocation();
  }, []);

  const requestPermissions = async () => {
    if (Platform.OS === 'android') {
      try {
        const granted = await PermissionsAndroid.requestMultiple([
          PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
          PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION,
        ]);
        
        if (
          granted[PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION] ===
            PermissionsAndroid.RESULTS.GRANTED &&
          granted[PermissionsAndroid.PERMISSIONS.ACCESS_COARSE_LOCATION] ===
            PermissionsAndroid.RESULTS.GRANTED
        ) {
          console.log('Location permissions granted');
          setIsLocationEnabled(true);
        } else {
          Alert.alert(
            'Permission Required',
            'Location permission is required for accurate compass readings'
          );
        }
      } catch (err) {
        console.warn(err);
      }
    }
  };

  const setupMagnetometer = () => {
    setUpdateIntervalForType(SensorTypes.magnetometer, 100);
    
    const subscription = magnetometer.subscribe(({x, y, z}) => {
      const data = {x, y, z};
      
      // Validate magnetometer data
      if (!validateMagnetometerData(data)) {
        return;
      }
      
      setMagnetometerData(data);
      
      // Calculate heading using utility function
      const newHeading = calculateHeading(data, location);
      setHeading(newHeading);
      
      // Apply smoothing to reduce jitter
      const smoothed = smoothHeading(newHeading, previousHeadingRef.current, 0.15);
      setSmoothedHeading(smoothed);
      previousHeadingRef.current = smoothed;
    });

    return () => subscription.unsubscribe();
  };

  const setupLocation = () => {
    if (isLocationEnabled) {
      Geolocation.getCurrentPosition(
        position => {
          const {latitude, longitude, accuracy} = position.coords;
          setLocation({latitude, longitude});
          setAccuracy(accuracy);
        },
        error => {
          console.log('Location error:', error);
        },
        {
          enableHighAccuracy: true,
          timeout: 15000,
          maximumAge: 10000,
        }
      );

      // Watch position for continuous updates
      const watchId = Geolocation.watchPosition(
        position => {
          const {latitude, longitude, accuracy} = position.coords;
          setLocation({latitude, longitude});
          setAccuracy(accuracy);
        },
        error => {
          console.log('Location watch error:', error);
        },
        {
          enableHighAccuracy: true,
          distanceFilter: 1,
          interval: 1000,
        }
      );

      return () => Geolocation.clearWatch(watchId);
    }
  };

  const getDetailedDirectionInfo = (heading) => {
    return getDetailedDirection(heading);
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1a1a2e" />
      
      <Header />
      
      <View style={styles.content}>
        <CompassComponent 
          heading={smoothedHeading}
          magnetometerData={magnetometerData}
        />
        
        <View style={styles.infoContainer}>
          <Text style={styles.headingText}>
            {Math.round(smoothedHeading)}°
          </Text>
          <Text style={styles.directionText}>
            {getDetailedDirectionInfo(smoothedHeading).direction}
          </Text>
          
          {location && (
            <LocationInfo 
              location={location}
              accuracy={accuracy}
            />
          )}
        </View>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a2e',
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  infoContainer: {
    alignItems: 'center',
    marginTop: 30,
  },
  headingText: {
    fontSize: 48,
    fontWeight: 'bold',
    color: '#fff',
    textAlign: 'center',
  },
  directionText: {
    fontSize: 24,
    color: '#e94560',
    textAlign: 'center',
    marginTop: 10,
  },
});

export default App;
