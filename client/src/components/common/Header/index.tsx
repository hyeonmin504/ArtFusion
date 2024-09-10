"use client";

import { usePathname } from "next/navigation";
import styles from "./header.module.scss";
import { log } from "console";
import Link from "next/link";
import clsx from "clsx";
// type Props = {
//     className?: string;
//     content?: string;
//     isAni?: boolean;
//     [key: string]: any;
//   };

export default function Header() {
  const pathname = usePathname();
  const isAuth =
    pathname != "/" && pathname != "/login" && pathname != "/register";
  console.log(pathname);
  // const activeId = pathname.startsWith("/temporary") && pathname.split("/")[1];
  // console.log(activeId);

  const pathArr = [
    { url: "/", name: "랜딩" },
    { url: "/login", name: "로그인" },
    { url: "/register", name: "회원가입" },
    { url: "/archives", name: "아카이브" },
    { url: "/storys", name: "생성" },
  ];
  const normalPaths = [
    { url: "/login", name: "로그인" },
    { url: "/register", name: "회원가입" },
  ];
  const authPaths = [
    { url: "/storys", name: "생성" },
    { url: "/archives", name: "아카이브" },
    { url: "/archives/my", name: "내 작품" },
  ];

  const renderNormalPaths = normalPaths.map((item) => (
    <Link
      className={clsx(styles["header-item"], {
        [styles["active"]]: pathname == item.url,
      })}
      href={item.url}
      key={item.name}
    >
      {item.name}
    </Link>
  ));

  const renderAuthPaths = authPaths.map((item) => (
    <Link
      className={clsx(styles["header-item"], {
        [styles["active"]]: pathname == item.url,
      })}
      href={item.url}
      key={item.name}
    >
      {item.name}
    </Link>
  ));
  return (
    <header className={styles.header}>
      <div className={styles["header-inner"]}>
        <div className={styles["header-inner-left"]}>
          <Link
            href={(isAuth && "/archives") || "/"}
            className={styles["header-logo"]}
          >
            ArtFusion
          </Link>
        </div>
        <div className={styles["header-inner-right"]}>
          {isAuth && renderAuthPaths}
          {!isAuth && renderNormalPaths}
        </div>
      </div>
    </header>
  );
}
