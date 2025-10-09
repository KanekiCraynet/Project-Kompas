import React from 'react';
import { View, StyleSheet } from 'react-native';
import Svg, { Line, Circle, Text as SvgText } from 'react-native-svg';

interface CompassNeedleProps {
  heading: number;
  size: number;
}

const CompassNeedle: React.FC<CompassNeedleProps> = ({ heading, size }) => {
  const center = size / 2;
  const needleLength = size * 0.35;
  const needleWidth = size * 0.02;

  // Calculate needle position based on heading
  const needleX = center + needleLength * Math.sin((heading * Math.PI) / 180);
  const needleY = center - needleLength * Math.cos((heading * Math.PI) / 180);

  // Calculate opposite end of needle
  const oppositeX = center - needleLength * 0.3 * Math.sin((heading * Math.PI) / 180);
  const oppositeY = center + needleLength * 0.3 * Math.cos((heading * Math.PI) / 180);

  return (
    <View style={[styles.container, { width: size, height: size }]}>
      <Svg width={size} height={size}>
        {/* North needle (red) */}
        <Line
          x1={center}
          y1={center}
          x2={needleX}
          y2={needleY}
          stroke="#ff4757"
          strokeWidth={needleWidth}
          strokeLinecap="round"
        />
        
        {/* South needle (white) */}
        <Line
          x1={center}
          y1={center}
          x2={oppositeX}
          y2={oppositeY}
          stroke="#ffffff"
          strokeWidth={needleWidth}
          strokeLinecap="round"
        />
        
        {/* Center circle */}
        <Circle
          cx={center}
          cy={center}
          r={size * 0.03}
          fill="#2c2c54"
          stroke="#fff"
          strokeWidth={2}
        />
        
        {/* North indicator */}
        <Circle
          cx={needleX}
          cy={needleY}
          r={size * 0.015}
          fill="#ff4757"
        />
        
        {/* South indicator */}
        <Circle
          cx={oppositeX}
          cy={oppositeY}
          r={size * 0.015}
          fill="#ffffff"
        />
      </Svg>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    position: 'absolute',
    alignItems: 'center',
    justifyContent: 'center',
  },
});

export default CompassNeedle;
