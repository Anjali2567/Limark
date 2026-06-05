import { IconDefinition } from '@fortawesome/fontawesome-svg-core';
import { faCode } from '@fortawesome/free-solid-svg-icons';
import {
  faJava,
  faReact,
  faNodeJs,
  faAngular,
  faVuejs,
  faPython,
  faPhp,
  faLaravel,
  faDocker,
  faAws,
  faGithub,
  faGitAlt,
  faHtml5,
  faCss3Alt,
  faJs,
} from '@fortawesome/free-brands-svg-icons';

import { faDatabase, faServer } from '@fortawesome/free-solid-svg-icons';

export const technologyIconMap: Record<string, IconDefinition> = {
  java: faJava,
  react: faReact,
  node: faNodeJs,
  angular: faAngular,
  vue: faVuejs,
  python: faPython,
  php: faPhp,
  laravel: faLaravel,
  docker: faDocker,
  aws: faAws,
  github: faGithub,
  git: faGitAlt,
  html: faHtml5,
  css: faCss3Alt,
  javascript: faJs,
  database: faDatabase,
  backend: faServer,
};

export const getTechnologyIcon = (key?: string): IconDefinition => {
  if (!key) return faCode;
  return technologyIconMap[key.toLowerCase()] ?? faCode;
};
