import clsx from "clsx";
import styles from "./main-section.module.scss";

type Props = {
  children?: React.ReactNode;
  className?: string;
};
export default function MainSection({ children, className }: Props) {
  return (
    <section className={clsx(styles["main-section"], className)}>
      {children}
    </section>
  );
}
