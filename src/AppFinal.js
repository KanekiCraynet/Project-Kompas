import React, {useState, useEffect, useRef} from 'react';
import {
  SafeAreaView,
  StyleSheet,
  StatusBar,
  View,
  Text,
  Dimensions,
  Alert,
} from 'react-native';
import {PermissionsAndroid, Platform} from 'react-native';
import Geolocation from 'react-native-geolocation-service';

const {width, height} = Dimensions.get('window');

const AppFinal = () => {
  const [heading, setHeading] = useState(0);
  const [location, setLocation] = useState(null);
  const [accuracy, setAccuracy] = useState(0);
  const [isLocationEnabled, setIsLocationEnabled] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);
  const [isInitialized, setIsInitialized] = useState(false);
  
  const locationWatchIdRef = useRef(null);
  const headingIntervalRef = useRef(null);

  useEffect(() => {
    initializeApp();
    
    return () => {
      cleanup();
    };
  }, []);

  const initializeApp = async () => {
    try {
      await requestPermissions();
      setupLocation();
      startHeadingSimulation();
      setIsInitialized(true);
    } catch (error) {
      console.error('Initialization error:', error);
      setErrorMessage('Gagal menginisialisasi aplikasi');
    }
  };

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
          console.log('Location permissions denied');
          setIsLocationEnabled(false);
        }
      } catch (err) {
        console.warn('Permission error:', err);
        setIsLocationEnabled(false);
      }
    } else {
      setIsLocationEnabled(true);
    }
  };

  const setupLocation = () => {
    if (!isLocationEnabled) return;
    
    try {
      Geolocation.getCurrentPosition(
        (position) => {
          const {latitude, longitude, accuracy} = position.coords;
          setLocation({latitude, longitude});
          setAccuracy(accuracy);
        },
        (error) => {
          console.log('Location error:', error);
        },
        {
          enableHighAccuracy: true,
          timeout: 15000,
          maximumAge: 10000,
        }
      );

      locationWatchIdRef.current = Geolocation.watchPosition(
        (position) => {
          const {latitude, longitude, accuracy} = position.coords;
          setLocation({latitude, longitude});
          setAccuracy(accuracy);
        },
        (error) => {
          console.log('Location watch error:', error);
        },
        {
          enableHighAccuracy: true,
          distanceFilter: 10,
          interval: 5000,
        }
      );
    } catch (error) {
      console.error('Failed to setup location:', error);
    }
  };

  const startHeadingSimulation = () => {
    // Simulate compass heading changes
    let currentHeading = 0;
    headingIntervalRef.current = setInterval(() => {
      currentHeading += 1;
      if (currentHeading >= 360) {
        currentHeading = 0;
      }
      setHeading(currentHeading);
    }, 100);
  };

  const cleanup = () => {
    try {
      if (locationWatchIdRef.current) {
        Geolocation.clearWatch(locationWatchIdRef.current);
        locationWatchIdRef.current = null;
      }
      if (headingIntervalRef.current) {
        clearInterval(headingIntervalRef.current);
        headingIntervalRef.current = null;
      }
    } catch (error) {
      console.error('Cleanup error:', error);
    }
  };

  const getDirectionName = (heading) => {
    const directions = [
      'Utara', 'Utara-Timur', 'Timur', 'Tenggara',
      'Selatan', 'Barat Daya', 'Barat', 'Barat Laut'
    ];
    
    const index = Math.round(heading / 45) % 8;
    return directions[index];
  };

  const renderCompass = () => {
    const compassSize = width * 0.6;
    const centerX = compassSize / 2;
    const centerY = compassSize / 2;
    const needleLength = compassSize / 2 - 20;
    
    return (
      <View style={[styles.compassContainer, {width: compassSize, height: compassSize}]}>
        {/* Compass circle */}
        <View style={[styles.compassCircle, {width: compassSize, height: compassSize, borderRadius: compassSize / 2}]}>
          {/* North indicator */}
          <View style={[styles.northIndicator, {top: 10}]}>
            <Text style={styles.northText}>N</Text>
          </View>
          
          {/* East indicator */}
          <View style={[styles.eastIndicator, {right: 10}]}>
            <Text style={styles.directionText}>E</Text>
          </View>
          
          {/* South indicator */}
          <View style={[styles.southIndicator, {bottom: 10}]}>
            <Text style={styles.directionText}>S</Text>
          </View>
          
          {/* West indicator */}
          <View style={[styles.westIndicator, {left: 10}]}>
            <Text style={styles.directionText}>W</Text>
          </View>
          
          {/* Needle */}
          <View 
            style={[
              styles.needle,
              {
                transform: [{rotate: `${heading}deg`}],
                left: centerX - 2,
                top: centerY - needleLength,
                height: needleLength * 2,
              }
            ]}
          >
            <View style={styles.needlePoint} />
          </View>
          
          {/* Center dot */}
          <View style={[styles.centerDot, {left: centerX - 5, top: centerY - 5}]} />
        </View>
      </View>
    );
  };

  if (!isInitialized) {
    return (
      <SafeAreaView style={styles.container}>
        <StatusBar barStyle="light-content" backgroundColor="#1a1a2e" />
        <View style={styles.loadingContainer}>
          <Text style={styles.loadingText}>Memuat Kompas...</Text>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1a1a2e" />
      
      <View style={styles.header}>
        <Text style={styles.headerText}>Kompas GPS</Text>
      </View>
      
      <View style={styles.content}>
        {errorMessage ? (
          <View style={styles.errorContainer}>
            <Text style={styles.errorText}>{errorMessage}</Text>
          </View>
        ) : (
          <>
            {renderCompass()}
            
            <View style={styles.infoContainer}>
              <Text style={styles.headingText}>
                {Math.round(heading)}°
              </Text>
              <Text style={styles.directionText}>
                {getDirectionName(heading)}
              </Text>
              
              {location && (
                <View style={styles.locationContainer}>
                  <Text style={styles.locationText}>
                    Lat: {location.latitude.toFixed(6)}
                  </Text>
                  <Text style={styles.locationText}>
                    Lng: {location.longitude.toFixed(6)}
                  </Text>
                  <Text style={styles.accuracyText}>
                    Akurasi: {accuracy.toFixed(1)}m
                  </Text>
                </View>
              )}
              
              <View style={styles.statusContainer}>
                <Text style={styles.statusText}>
                  Mode Demo - Sensor Simulasi
                </Text>
                <Text style={styles.instructionText}>
                  Aplikasi berjalan dengan simulasi kompas
                </Text>
              </View>
            </View>
          </>
        )}
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a2e',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    fontSize: 18,
    color: '#fff',
    textAlign: 'center',
  },
  header: {
    padding: 20,
    alignItems: 'center',
  },
  headerText: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#fff',
  },
  content: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  compassContainer: {
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 30,
  },
  compassCircle: {
    borderWidth: 3,
    borderColor: '#e94560',
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    position: 'relative',
  },
  northIndicator: {
    position: 'absolute',
    alignItems: 'center',
  },
  eastIndicator: {
    position: 'absolute',
    alignItems: 'center',
    top: '50%',
    marginTop: -10,
  },
  southIndicator: {
    position: 'absolute',
    alignItems: 'center',
    left: '50%',
    marginLeft: -10,
  },
  westIndicator: {
    position: 'absolute',
    alignItems: 'center',
    top: '50%',
    marginTop: -10,
  },
  northText: {
    color: '#e94560',
    fontSize: 18,
    fontWeight: 'bold',
  },
  directionText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: 'bold',
  },
  needle: {
    position: 'absolute',
    width: 4,
    backgroundColor: '#e94560',
    borderRadius: 2,
  },
  needlePoint: {
    position: 'absolute',
    top: 0,
    left: -3,
    width: 0,
    height: 0,
    borderLeftWidth: 5,
    borderRightWidth: 5,
    borderBottomWidth: 15,
    borderLeftColor: 'transparent',
    borderRightColor: 'transparent',
    borderBottomColor: '#e94560',
  },
  centerDot: {
    position: 'absolute',
    width: 10,
    height: 10,
    backgroundColor: '#fff',
    borderRadius: 5,
  },
  infoContainer: {
    alignItems: 'center',
  },
  headingText: {
    fontSize: 48,
    fontWeight: 'bold',
    color: '#fff',
    textAlign: 'center',
  },
  locationContainer: {
    marginTop: 20,
    padding: 15,
    backgroundColor: 'rgba(255, 255, 255, 0.1)',
    borderRadius: 10,
    alignItems: 'center',
  },
  locationText: {
    fontSize: 14,
    color: '#ccc',
    marginBottom: 5,
  },
  accuracyText: {
    fontSize: 14,
    color: '#4CAF50',
    fontWeight: 'bold',
  },
  statusContainer: {
    marginTop: 15,
    padding: 10,
    backgroundColor: 'rgba(255, 193, 7, 0.2)',
    borderRadius: 8,
    alignItems: 'center',
  },
  statusText: {
    fontSize: 12,
    color: '#ffc107',
    fontWeight: 'bold',
  },
  instructionText: {
    fontSize: 10,
    color: '#ccc',
    textAlign: 'center',
    marginTop: 2,
  },
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  errorText: {
    fontSize: 18,
    color: '#ff6b6b',
    textAlign: 'center',
    fontWeight: 'bold',
  },
});

export default AppFinal;
