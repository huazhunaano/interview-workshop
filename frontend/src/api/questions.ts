/**
 * 岗位题库 API（issue #1：题库从后端取，不再只写在前端 mock）。
 *
 * 端点：
 * - `GET /api/questions?jobCode=java&type=technical&limit=20`
 *
 * 字段与后端 `QuestionView` 一一对应；`typeLabel` 是中文题型，前端直接当展示文案用。
 * 写法与 `api/evaluation.ts` 一致：原生 fetch，非 2xx 抛带后端文案的 Error。
 */

/** 题库中的一道题，对应后端 `QuestionView`。 */
export interface QuestionItem {
  id: number;
  code: string;
  jobCode: string;
  kpCode: string | null;
  typeCode: string;
  typeLabel: string;
  difficulty: string | null;
  text: string;
  reference: string | null;
  followUps: string[];
}

/** 从失败响应里挖出后端文案（`detail` 优先），拿不到就退回状态码。 */
async function readError(response: Response): Promise<string> {
  try {
    const text = await response.text();
    if (text) {
      try {
        const body = JSON.parse(text) as { detail?: string; message?: string; error?: string };
        const message = body.detail ?? body.message ?? body.error;
        if (message) return message;
      } catch {
        return text.slice(0, 200);
      }
    }
  } catch {
    // 读 body 失败就退回状态码
  }
  return `HTTP ${response.status}`;
}

/**
 * 取某岗位的题库。
 *
 * @param jobCode 岗位编码（java / frontend）
 * @param opts.type 题型，可选；英文 code 或中文标签都认
 * @param opts.limit 条数，后端默认 20、上限 100
 */
export async function fetchQuestions(
  jobCode: string,
  opts?: { type?: string; limit?: number },
): Promise<QuestionItem[]> {
  const params = new URLSearchParams({ jobCode });
  if (opts?.type) params.set("type", opts.type);
  if (opts?.limit != null) params.set("limit", String(opts.limit));

  let response: Response;
  try {
    response = await fetch(`/api/questions?${params.toString()}`);
  } catch (e) {
    throw new Error(e instanceof Error ? e.message : "无法连接后端");
  }
  if (!response.ok) throw new Error(await readError(response));
  return (await response.json()) as QuestionItem[];
}
