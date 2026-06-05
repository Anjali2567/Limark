export enum VendorVerificationStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  INCOMPLETE = 'INCOMPLETE',
}

export const vendorVerificationStatusOptions = [
  { label: 'Pending', value: VendorVerificationStatus.PENDING },
  { label: 'Approved', value: VendorVerificationStatus.APPROVED },
  { label: 'Rejected', value: VendorVerificationStatus.REJECTED },
  { label: 'Incomplete', value: VendorVerificationStatus.INCOMPLETE },
];
