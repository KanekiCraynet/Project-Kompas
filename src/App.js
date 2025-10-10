import React, {useState, useEffect} from 'react';
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

const {width, height} = Dimensions.get('window');

const App = () => {
  const [magnetometerData, setMagnetometerData] = useState({x: 0, y: 0, z: 0});
  const [heading, setHeading] = useState(0);
  const [location, setLocation] = useState(null);
  const [accuracy, setAccuracy] = useState(0);
  const [isLocationEnabled, setIsLocationEnabled] = useState(false);

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
      setMagnetometerData({x, y, z});
      
      // Calculate heading from magnetometer data
      let angle = Math.atan2(y, x) * (180 / Math.PI);
      angle = angle < 0 ? angle + 360 : angle;
      
      // Apply magnetic declination correction (simplified)
      const magneticDeclination = getMagneticDeclination(location);
      angle = angle + magneticDeclination;
      
      setHeading(angle);
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

  const getMagneticDeclination = (location) => {
    if (!location) return 0;
    
    // Simplified magnetic declination calculation
    // In a real app, you would use a proper magnetic declination service
    const {latitude, longitude} = location;
    
    // Basic approximation for Indonesia region
    if (latitude >= -11 && latitude <= 6 && longitude >= 95 && longitude <= 141) {
      return 0.5; // Approximate declination for Indonesia
    }
    
    return 0;
  };

  const getDirectionName = (heading) => {
    const directions = [
      'Utara', 'Utara-Timur', 'Timur', 'Tenggara',
      'Selatan', 'Barat Daya', 'Barat', 'Barat Laut'
    ];
    
    const index = Math.round(heading / 45) % 8;
    return directions[index];
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1a1a2e" />
      
      <Header />
      
      <View style={styles.content}>
        <CompassComponent 
          heading={heading}
          magnetometerData={magnetometerData}
        />
        
        <View style={styles.infoContainer}>
          <Text style={styles.headingText}>
            {Math.round(heading)}°
          </Text>
          <Text style={styles.directionText}>
            {getDirectionName(heading)}
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
