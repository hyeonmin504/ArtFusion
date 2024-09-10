import clsx from "clsx";
import styles from "./modal-container.module.scss";
type Props = {
  children?: React.ReactNode;
  className?: string;
  type?: string;
  [key: string]: any;
};
export default function ModalContainer({
  children,
  className,
  type = "normal",
  ...rest
}: Props) {
  return (
    <article
      className={clsx(styles.container, className, styles[type])}
      {...rest}
    >
      {children}
    </article>
  );
}
