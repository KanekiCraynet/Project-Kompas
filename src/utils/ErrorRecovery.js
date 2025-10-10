/**
 * Error Recovery and Fallback Mechanisms for Compass App
 * Handles various error scenarios and provides recovery options
 */

import { Alert } from 'react-native';

// Error types and their recovery strategies
export const ERROR_TYPES = {
  SENSOR_UNAVAILABLE: 'SENSOR_UNAVAILABLE',
  PERMISSION_DENIED: 'PERMISSION_DENIED',
  LOCATION_ERROR: 'LOCATION_ERROR',
  MEMORY_ERROR: 'MEMORY_ERROR',
  NETWORK_ERROR: 'NETWORK_ERROR',
  UNKNOWN_ERROR: 'UNKNOWN_ERROR'
};

// Error recovery strategies
export const RECOVERY_STRATEGIES = {
  RETRY: 'RETRY',
  FALLBACK: 'FALLBACK',
  RESTART: 'RESTART',
  IGNORE: 'IGNORE',
  USER_ACTION: 'USER_ACTION'
};

/**
 * Error Recovery Manager
 */
export class ErrorRecoveryManager {
  constructor() {
    this.errorCounts = {};
    this.maxRetries = 3;
    this.retryDelays = [1000, 2000, 5000]; // Progressive delays
  }

  /**
   * Handle error with appropriate recovery strategy
   * @param {string} errorType - Type of error
   * @param {Error} error - Error object
   * @param {Function} recoveryCallback - Callback to execute recovery
   * @returns {Promise<boolean>} - Whether recovery was successful
   */
  async handleError(errorType, error, recoveryCallback) {
    try {
      console.log(`Handling error: ${errorType}`, error);
      
      // Increment error count
      this.errorCounts[errorType] = (this.errorCounts[errorType] || 0) + 1;
      
      // Determine recovery strategy
      const strategy = this.getRecoveryStrategy(errorType, this.errorCounts[errorType]);
      
      // Execute recovery
      const success = await this.executeRecovery(strategy, errorType, error, recoveryCallback);
      
      if (success) {
        // Reset error count on successful recovery
        this.errorCounts[errorType] = 0;
      }
      
      return success;
    } catch (recoveryError) {
      console.error('Error in error recovery:', recoveryError);
      return false;
    }
  }

  /**
   * Get recovery strategy based on error type and count
   * @param {string} errorType - Type of error
   * @param {number} errorCount - Number of times this error occurred
   * @returns {string} - Recovery strategy
   */
  getRecoveryStrategy(errorType, errorCount) {
    if (errorCount <= this.maxRetries) {
      return RECOVERY_STRATEGIES.RETRY;
    }
    
    switch (errorType) {
      case ERROR_TYPES.SENSOR_UNAVAILABLE:
        return RECOVERY_STRATEGIES.FALLBACK;
      case ERROR_TYPES.PERMISSION_DENIED:
        return RECOVERY_STRATEGIES.USER_ACTION;
      case ERROR_TYPES.LOCATION_ERROR:
        return RECOVERY_STRATEGIES.FALLBACK;
      case ERROR_TYPES.MEMORY_ERROR:
        return RECOVERY_STRATEGIES.RESTART;
      case ERROR_TYPES.NETWORK_ERROR:
        return RECOVERY_STRATEGIES.IGNORE;
      default:
        return RECOVERY_STRATEGIES.FALLBACK;
    }
  }

  /**
   * Execute recovery strategy
   * @param {string} strategy - Recovery strategy
   * @param {string} errorType - Type of error
   * @param {Error} error - Error object
   * @param {Function} recoveryCallback - Callback to execute recovery
   * @returns {Promise<boolean>} - Whether recovery was successful
   */
  async executeRecovery(strategy, errorType, error, recoveryCallback) {
    switch (strategy) {
      case RECOVERY_STRATEGIES.RETRY:
        return await this.retryWithDelay(errorType, recoveryCallback);
      
      case RECOVERY_STRATEGIES.FALLBACK:
        return await this.executeFallback(errorType, error);
      
      case RECOVERY_STRATEGIES.RESTART:
        return await this.restartApplication();
      
      case RECOVERY_STRATEGIES.USER_ACTION:
        return await this.requestUserAction(errorType, error);
      
      case RECOVERY_STRATEGIES.IGNORE:
        return true; // Ignore the error
      
      default:
        return false;
    }
  }

  /**
   * Retry with progressive delay
   * @param {string} errorType - Type of error
   * @param {Function} recoveryCallback - Callback to execute recovery
   * @returns {Promise<boolean>} - Whether retry was successful
   */
  async retryWithDelay(errorType, recoveryCallback) {
    const errorCount = this.errorCounts[errorType];
    const delay = this.retryDelays[Math.min(errorCount - 1, this.retryDelays.length - 1)];
    
    console.log(`Retrying ${errorType} in ${delay}ms (attempt ${errorCount})`);
    
    return new Promise((resolve) => {
      setTimeout(async () => {
        try {
          const result = await recoveryCallback();
          resolve(result);
        } catch (retryError) {
          console.error(`Retry failed for ${errorType}:`, retryError);
          resolve(false);
        }
      }, delay);
    });
  }

  /**
   * Execute fallback behavior
   * @param {string} errorType - Type of error
   * @param {Error} error - Error object
   * @returns {Promise<boolean>} - Whether fallback was successful
   */
  async executeFallback(errorType, error) {
    console.log(`Executing fallback for ${errorType}`);
    
    switch (errorType) {
      case ERROR_TYPES.SENSOR_UNAVAILABLE:
        // Use simulated compass data
        return true;
      
      case ERROR_TYPES.LOCATION_ERROR:
        // Continue without location data
        return true;
      
      case ERROR_TYPES.NETWORK_ERROR:
        // Continue without network features
        return true;
      
      default:
        return false;
    }
  }

  /**
   * Restart application (simulated)
   * @returns {Promise<boolean>} - Whether restart was successful
   */
  async restartApplication() {
    console.log('Restarting application...');
    
    return new Promise((resolve) => {
      Alert.alert(
        'Restart Diperlukan',
        'Aplikasi perlu di-restart untuk mengatasi masalah. Apakah Anda ingin melanjutkan?',
        [
          {
            text: 'Batal',
            style: 'cancel',
            onPress: () => resolve(false)
          },
          {
            text: 'Restart',
            onPress: () => {
              // In a real app, you would restart the app here
              // For now, we'll just resolve as successful
              resolve(true);
            }
          }
        ]
      );
    });
  }

  /**
   * Request user action to resolve error
   * @param {string} errorType - Type of error
   * @param {Error} error - Error object
   * @returns {Promise<boolean>} - Whether user action was successful
   */
  async requestUserAction(errorType, error) {
    console.log(`Requesting user action for ${errorType}`);
    
    return new Promise((resolve) => {
      let title, message, actions;
      
      switch (errorType) {
        case ERROR_TYPES.PERMISSION_DENIED:
          title = 'Izin Diperlukan';
          message = 'Aplikasi memerlukan izin lokasi untuk berfungsi dengan optimal. Silakan aktifkan izin di pengaturan.';
          actions = [
            { text: 'Batal', style: 'cancel', onPress: () => resolve(false) },
            { text: 'Pengaturan', onPress: () => resolve(true) }
          ];
          break;
        
        default:
          title = 'Aksi Diperlukan';
          message = 'Terjadi masalah yang memerlukan aksi dari Anda.';
          actions = [
            { text: 'OK', onPress: () => resolve(true) }
          ];
      }
      
      Alert.alert(title, message, actions);
    });
  }

  /**
   * Reset error counts
   */
  resetErrorCounts() {
    this.errorCounts = {};
  }

  /**
   * Get error statistics
   * @returns {Object} - Error statistics
   */
  getErrorStats() {
    return {
      totalErrors: Object.values(this.errorCounts).reduce((sum, count) => sum + count, 0),
      errorTypes: { ...this.errorCounts },
      isHealthy: Object.values(this.errorCounts).every(count => count < this.maxRetries)
    };
  }
}

// Global error recovery manager instance
export const errorRecoveryManager = new ErrorRecoveryManager();

/**
 * Utility function to handle errors with recovery
 * @param {string} errorType - Type of error
 * @param {Error} error - Error object
 * @param {Function} recoveryCallback - Callback to execute recovery
 * @returns {Promise<boolean>} - Whether recovery was successful
 */
export const handleErrorWithRecovery = async (errorType, error, recoveryCallback) => {
  return await errorRecoveryManager.handleError(errorType, error, recoveryCallback);
};

/**
 * Utility function to check if device has required sensors
 * @returns {Promise<Object>} - Sensor availability status
 */
export const checkSensorAvailability = async () => {
  try {
    // This would normally check actual sensor availability
    // For now, we'll return a mock response
    return {
      magnetometer: true,
      accelerometer: true,
      gyroscope: false,
      location: true
    };
  } catch (error) {
    console.error('Error checking sensor availability:', error);
    return {
      magnetometer: false,
      accelerometer: false,
      gyroscope: false,
      location: false
    };
  }
};

/**
 * Utility function to get fallback compass data
 * @returns {Object} - Fallback compass data
 */
export const getFallbackCompassData = () => {
  return {
    x: 25.5,
    y: 15.2,
    z: -45.8,
    heading: 0,
    accuracy: 0.3,
    isFallback: true
  };
};
