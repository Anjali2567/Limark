import { FC, PropsWithChildren } from 'react';
import { AnimatePresence, motion } from 'framer-motion';

type AnimatedCollapseProps = {
  isExpanded: boolean;
};
const AnimatedCollapse: FC<PropsWithChildren<AnimatedCollapseProps>> = ({
  isExpanded,
  children,
}) => {
  return (
    <AnimatePresence initial={false}>
      {isExpanded && (
        <motion.div
          key="expanded-section"
          initial={{ opacity: 0, height: 0 }}
          animate={{ opacity: 1, height: 'auto' }}
          exit={{ opacity: 0, height: 0 }}
          transition={{ duration: 0.2, ease: 'easeOut' }}
          layout
        >
          {children}
        </motion.div>
      )}
    </AnimatePresence>
  );
};

export { AnimatedCollapse };
