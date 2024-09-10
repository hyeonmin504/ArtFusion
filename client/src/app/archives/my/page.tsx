"use client";

import MainSection from "@/components/common/MainSection";
import styles from "./my.module.scss";
import ArchiveContainer from "@/components/archives/ArchiveContainer";
import AddLink from "@/components/archives/my/AddLink";
import { useQuery } from "@tanstack/react-query";
export default function Page() {
  const requestUrl = `${process.env.NEXT_PUBLIC_SERVER_URL}/archives/my`;
  //GET
  const { isPending, data, isError, refetch, error } = useQuery({
    queryKey: ["archives-my"],
    queryFn: async () => {
      const response = await fetch(requestUrl, { credentials: "include" });

      if (!response.ok) {
        throw new Error("Network response was not ok");
      }
      return response.json();
    },
    retry: false,
  });
  console.log(data);

  return (
    <MainSection className={styles["archive-my-section"]}>
      <div className={styles["archive-my-sec-title"]}>
        <span>글쟁이</span> 작가님 안녕하세요!
      </div>
      <p className={styles["archive-my-sec-dscr"]}>
        새로운 작품을 만들어볼까요?
      </p>
      <div className={styles["archive-my-sec-main"]}>
        <AddLink />
        <ArchiveContainer />
        <ArchiveContainer />
      </div>
    </MainSection>
  );
}
