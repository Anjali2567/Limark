export type ApolloSpecification = {
  id: string;
  personTitleEnabled: boolean;
  personTitles: string[];
  personSeniorityEnabled: boolean;
  personSeniorities: string[];
  createdAt: string;
  createdBy: string;
};

export type ApolloSpecificationRequest = {
  personTitleEnabled: boolean;
  personTitles: string[];
  personSeniorityEnabled: boolean;
  personSeniorities: string[];
};
