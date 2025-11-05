import { CommunityStats } from "../types";

export const mockCommunityStats: CommunityStats = {
  activeMembers: 247,
  hoursExchanged: 1438,
  activeServices: 89,
  completedThisMonth: 156,
};

export const getCommunityStats = (): CommunityStats => {
  return mockCommunityStats;
};

