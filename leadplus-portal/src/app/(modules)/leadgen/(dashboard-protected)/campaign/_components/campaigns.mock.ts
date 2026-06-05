export type CampaignStatus = "Running" | "Paused" | "Completed" | "Pending";

export interface CampaignListResponse {
  id: string;
  name: string;
  industry: string;
  lastModified: string;
  totalContacts: number;
  sent: number;
  opened: number;
  progress: number;
  status: CampaignStatus;
}

export const campaigns: CampaignListResponse[] = [
  {
    id: "cmp-001",
    name: "Q4 Robotics Outreach",
    industry: "Robotics & Automation",
    lastModified: "2024-12-15",
    totalContacts: 150,
    sent: 450,
    opened: 285,
    progress: 75,
    status: "Running",
  },
  {
    id: "cmp-002",
    name: "Medical Devices Expansion",
    industry: "Medical Devices",
    lastModified: "2024-12-14",
    totalContacts: 320,
    sent: 960,
    opened: 672,
    progress: 85,
    status: "Running",
  },
  {
    id: "cmp-003",
    name: "Automotive Tier 1",
    industry: "Automotive Manufacturing",
    lastModified: "2024-12-10",
    totalContacts: 85,
    sent: 170,
    opened: 102,
    progress: 45,
    status: "Paused",
  },
  {
    id: "cmp-004",
    name: "SaaS Partnership Alpha",
    industry: "Consumer Electronics",
    lastModified: "2024-11-28",
    totalContacts: 200,
    sent: 600,
    opened: 420,
    progress: 100,
    status: "Completed",
  },
  {
    id: "cmp-005",
    name: "Aerospace Component Suppliers",
    industry: "Aerospace",
    lastModified: "2024-12-16",
    totalContacts: 120,
    sent: 240,
    opened: 156,
    progress: 65,
    status: "Running",
  },
  {
    id: "cmp-006",
    name: "Eco-Friendly Packaging",
    industry: "Manufacturing",
    lastModified: "2024-12-17",
    totalContacts: 450,
    sent: 1350,
    opened: 945,
    progress: 90,
    status: "Running",
  },
  {
    id: "cmp-007",
    name: "Textile Machinery 2024",
    industry: "Textiles",
    lastModified: "2024-11-20",
    totalContacts: 95,
    sent: 285,
    opened: 200,
    progress: 100,
    status: "Completed",
  },
  {
    id: "cmp-008",
    name: "Healthcare Innovation Drive",
    industry: "Healthcare",
    lastModified: "2024-12-16",
    totalContacts: 180,
    sent: 0,
    opened: 0,
    progress: 0,
    status: "Pending",
  },
];
