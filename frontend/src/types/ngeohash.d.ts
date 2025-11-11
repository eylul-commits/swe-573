declare module 'ngeohash' {
  export interface GeohashCoordinates {
    latitude: number;
    longitude: number;
  }

  export function encode(latitude: number, longitude: number, precision?: number): string;
  export function decode(geohash: string): GeohashCoordinates;
  export function decode_bbox(geohash: string): number[];
  export function neighbor(geohash: string, direction: number[]): string;
  export function neighbors(geohash: string): string[];
}

