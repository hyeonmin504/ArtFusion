import clsx from "clsx";
import styles from "./button.module.scss";
import Link from "next/link";

type Props = {
  onClick?: () => void;
  color?: string;
  type?: string;
  isActive?: boolean;
  isLoading?: boolean;
  className?: string;
  height?: number;
  width?: number;
  href?: string;
  children?: React.ReactNode;
};

export default function Button({
  onClick,
  isActive = false,
  color = "primary",
  type = "normal",
  width,
  height,
  isLoading,
  className,
  children,
  href = "/",
}: Props) {
  return type == "link" ? (
    <Link
      className={clsx(styles.button, styles[color], styles[type], className, {
        [styles.loading]: isLoading,
        [styles.active]: isActive,
      })}
      href={href}
      style={{ width, height }}
    >
      {children}
    </Link>
  ) : (
    <button
      className={clsx(styles.button, styles[color], styles[type], className, {
        [styles.loading]: isLoading,
        [styles.active]: isActive,
      })}
      style={{ width, height }}
      onClick={onClick}
      disabled={isLoading}
    >
      {isLoading && (
        <div className={styles["btn-inner"]}>
          <div className={styles.loader}></div>
        </div>
      )}
      {!isLoading && children}
    </button>
  );
}
