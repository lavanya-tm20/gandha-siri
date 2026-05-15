export interface Tree {
  id: string;
  name: string;
  age: number;
  girth: number;
  farmerName: string;
  location: {
    lat: number;
    lng: number;
  };
  photoUrl?: string;
  notes?: string;
  userId: string;
  createdAt: string;
}

export interface GrowthRecord {
  id: string;
  treeId: string;
  girth: number;
  date: string;
  userId: string;
  createdAt: string;
}

export interface Alert {
  id: string;
  type: 'theft' | 'suspicious' | 'other';
  location?: {
    lat: number;
    lng: number;
  };
  userId: string;
  status: 'pending' | 'resolved';
  createdAt: string;
}

export interface UserProfile {
  userId: string;
  email: string;
  name?: string;
  role: 'farmer' | 'admin';
  createdAt: string;
}
