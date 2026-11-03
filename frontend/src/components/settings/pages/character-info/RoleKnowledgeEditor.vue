<template>
  <div ref="rootRef" class="space-y-3 rounded-xl border border-white/10 bg-black/20 p-4">
    <div class="flex items-start justify-between gap-3">
      <div class="min-w-0">
        <h3 class="text-sm font-bold text-white/80">岗位知识</h3>
        <p class="mt-1 text-[11px] leading-relaxed text-white/40">
          勾选的文档会在面试时作为该考官的提问依据（RAG 检索后按轮注入）。
        </p>
      </div>
      <button class="btn-sm shrink-0" :disabled="saving || loading" @click="save">
        {{ saving ? "保存中..." : "保存勾选" }}
      </button>
    </div>

    <div v-if="loading" class="text-xs text-white/40">加载中...</div>

    <template v-else>
      <!-- 多选下拉：收起时只显示已选数量，展开后按分类列出全部文档 -->
      <div class="relative">
        <button
          type="button"
          class="flex w-full cursor-pointer items-center justify-between gap-2 rounded-xl border border-white/10 bg-black/20 px-3.5 py-2.5 text-left text-sm text-white/80 transition-colors hover:bg-white/5"
          :aria-expanded="open"
          @click="open = !open"
        >
          <span class="min-w-0 truncate">
            <span v-if="selectedDocs.length === 0" class="text-white/40">未绑定任何知识库</span>
            <span v-else>已绑定 {{ selectedDocs.length }} 本：{{ selectedDocs.map((d) => d.title).join("、") }}</span>
          </span>
          <ChevronDown
            :size="16"
            class="shrink-0 text-white/40 transition-transform"
            :class="{ 'rotate-180': open }"
          />
        </button>

        <div
          v-if="open"
          class="absolute inset-x-0 z-30 mt-1 rounded-xl border border-white/15 bg-[#1c1c24]/95 p-2 shadow-[0_18px_40px_rgba(0,0,0,0.45)] backdrop-blur"
        >
          <input v-model="docFilter" class="glass-input mb-2 text-xs" placeholder="搜索标题或编码" />

          <div v-if="docs.length > 0" class="max-h-56 space-y-1 overflow-y-auto">
            <label
              v-for="doc in filteredDocs"
              :key="doc.code"
              class="flex cursor-pointer items-center gap-2 rounded-lg px-2 py-1.5 text-sm text-white/80 hover:bg-white/5"
            >
              <input v-model="checked" type="checkbox" :value="doc.code" class="shrink-0 cursor-pointer" />
              <span class="min-w-0 flex-1 truncate">{{ doc.title }}</span>
              <span class="shrink-0 text-[11px] text-white/35">{{ doc.chunkCount }} 段</span>
            </label>
            <p v-if="filteredDocs.length === 0" class="px-2 py-1 text-xs text-white/40">
              没有匹配的文档
            </p>
          </div>
          <p v-else class="px-2 py-1 text-xs text-white/40">
            还没有导入任何文档，用下方「新增知识库」添加。
          </p>

          <div class="mt-2 flex items-center justify-between border-t border-white/10 pt-2">
            <button type="button" class="cursor-pointer text-[11px] text-white/45 hover:text-white/80" @click="checked = []">
              清空选择
            </button>
            <span class="text-[11px] text-white/40">已选 {{ checked.length }} 本</span>
          </div>
        </div>
      </div>

      <!-- 已选文档的标签，方便确认当前挂载 -->
      <div v-if="selectedDocs.length > 0" class="flex flex-wrap gap-1.5">
        <span
          v-for="doc in selectedDocs"
          :key="doc.code"
          class="rounded-full border border-white/15 bg-white/10 px-2.5 py-0.5 text-[11px] text-white/75"
        >
          {{ doc.code }}
        </span>
      </div>

      <!-- 新增知识库：支持 .md / .txt，后端切块 + 向量化后自动勾选 -->
      <div class="rounded-xl border border-dashed border-white/15 bg-white/5 p-3">
        <button
          type="button"
          class="flex w-full cursor-pointer items-center gap-2 text-left text-xs font-medium text-white/70 hover:text-white"
          @click="showAdd = !showAdd"
        >
          <Plus :size="14" class="shrink-0" />
          新增知识库（支持 Markdown / TXT）
          <ChevronDown
            :size="14"
            class="ml-auto shrink-0 text-white/40 transition-transform"
            :class="{ 'rotate-180': showAdd }"
          />
        </button>

        <div v-if="showAdd" class="mt-3 space-y-2">
          <div class="grid grid-cols-1 gap-2 sm:grid-cols-2">
            <input v-model="addForm.code" class="glass-input text-sm" placeholder="文档编码，如 java-mysql-index-notes" />
            <input v-model="addForm.title" class="glass-input text-sm" placeholder="文档标题" />
            <input v-model="addForm.category" class="glass-input text-sm" placeholder="岗位分类，可选：java / frontend" />
            <select v-model="addForm.format" class="glass-input cursor-pointer text-sm">
              <option value="md">Markdown（.md）</option>
              <option value="txt">纯文本（.txt）</option>
            </select>
          </div>

          <input
            ref="fileInput"
            type="file"
            accept=".md,.markdown,.txt,text/markdown,text/plain"
            class="text-xs text-white/60"
            @change="onFileChange"
          />
          <textarea
            v-model="addForm.text"
            rows="4"
            class="glass-input font-mono text-xs leading-relaxed"
            placeholder="或者直接粘贴正文（可选）。选择文件时以文件内容为准。"
          ></textarea>

          <div class="flex flex-wrap items-center gap-2">
            <button class="btn-sm" :disabled="!canAdd || adding" @click="addDoc">
              {{ adding ? "切分入库中..." : "添加并切分入库" }}
            </button>
            <span class="text-[11px] text-white/40">
              {{ file ? `已选文件 ${file.name}` : "未选文件时按上方格式切分粘贴内容" }}
            </span>
          </div>
          <p class="text-[11px] leading-relaxed text-white/40">
            Markdown 按 # / ## 标题切块；TXT 按空行分段切块。入库后自动勾选到当前角色，点上方「保存勾选」生效。
          </p>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from "vue";
import { ChevronDown, Plus } from "@lucide/vue";
import {
  getRoleKnowledge,
  listKnowledgeDocs,
  saveRoleKnowledge,
  uploadMarkdown,
  uploadText,
  type KnowledgeDocView,
} from "../../../../api/knowledge";
import { notify } from "../../../../store/appStore";
import { alertDialog } from "../../../../store/dialogStore";

const props = defineProps<{
  /** 角色 code（如 strict）；后端 interviewers/{code}/knowledge 按它存 */
  interviewerCode: string;
}>();

const docs = ref<KnowledgeDocView[]>([]);
const checked = ref<string[]>([]);
const loading = ref(false);
const saving = ref(false);

/** 下拉框展开状态与筛选词 */
const open = ref(false);
const docFilter = ref("");
const rootRef = ref<HTMLElement | null>(null);

const selectedDocs = computed(() => docs.value.filter((doc) => checked.value.includes(doc.code)));

const filteredDocs = computed(() => {
  const keyword = docFilter.value.trim().toLowerCase();
  if (!keyword) return docs.value;
  return docs.value.filter(
    (doc) =>
      doc.title.toLowerCase().includes(keyword) || doc.code.toLowerCase().includes(keyword),
  );
});

// 点空白处收起下拉，避免遮挡下面的表单
const onDocumentClick = (event: MouseEvent) => {
  if (open.value && !rootRef.value?.contains(event.target as Node)) {
    open.value = false;
  }
};
onMounted(() => document.addEventListener("click", onDocumentClick));
onUnmounted(() => document.removeEventListener("click", onDocumentClick));

// 换角色时旧请求可能后返回，用序号丢弃过期结果（同 RelationEditor 的写法）
let loadSeq = 0;

const load = async (interviewerCode: string) => {
  const seq = ++loadSeq;
  loading.value = true;
  try {
    const [allDocs, selected] = await Promise.all([
      listKnowledgeDocs(),
      getRoleKnowledge(interviewerCode),
    ]);
    if (seq !== loadSeq) return;
    docs.value = allDocs;
    checked.value = selected;
  } catch (e) {
    if (seq !== loadSeq) return;
    console.error("读取岗位知识失败:", e);
    void alertDialog("读取岗位知识失败", e instanceof Error ? e.message : String(e));
    docs.value = [];
    checked.value = [];
  } finally {
    if (seq === loadSeq) loading.value = false;
  }
};

const save = async () => {
  if (saving.value) return;
  saving.value = true;
  try {
    await saveRoleKnowledge(props.interviewerCode, checked.value);
    notify("已保存岗位知识");
    open.value = false;
  } catch (e) {
    void alertDialog("保存岗位知识失败", e instanceof Error ? e.message : String(e));
  } finally {
    saving.value = false;
  }
};

// ---------- 新增知识库 ----------

const showAdd = ref(false);
const adding = ref(false);
const fileInput = ref<HTMLInputElement | null>(null);
const file = ref<File | null>(null);
const addForm = reactive({
  code: "",
  title: "",
  /** md | txt；选了文件时按后缀自动改 */
  format: "md" as "md" | "txt",
  category: "",
  text: "",
});

const canAdd = computed(
  () =>
    !adding.value &&
    addForm.code.trim() !== "" &&
    addForm.title.trim() !== "" &&
    (file.value !== null || addForm.text.trim() !== ""),
);

function onFileChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const picked = input.files?.[0] ?? null;
  file.value = picked;
  if (!picked) return;
  const isTxt = /\.(txt|text)$/i.test(picked.name);
  addForm.format = isTxt ? "txt" : "md";
  // 编码没填时用文件名兜底，省得每个文件都手打一遍
  if (!addForm.code.trim()) {
    addForm.code = picked.name
      .replace(/\.(md|markdown|txt|text)$/i, "")
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, "-")
      .replace(/^-+|-+$/g, "");
  }
}

function resetAddForm() {
  addForm.code = "";
  addForm.title = "";
  addForm.category = "";
  addForm.text = "";
  addForm.format = "md";
  file.value = null;
  if (fileInput.value) fileInput.value.value = "";
}

const addDoc = async () => {
  if (!canAdd.value) return;
  const code = addForm.code.trim();
  adding.value = true;
  try {
    const picked = file.value;
    const text = picked ? await picked.text() : addForm.text;
    const payload = {
      title: addForm.title.trim(),
      category: addForm.category.trim() || undefined,
      sourcePath: picked?.name,
    };
    const result = picked && addForm.format === "txt"
      ? await uploadText(code, { ...payload, text })
      : await uploadMarkdown(code, { ...payload, markdown: text });

    // 新文档立即出现在下拉里，并自动勾上；保存后即生效
    await load(props.interviewerCode);
    if (!checked.value.includes(code)) {
      checked.value = [...checked.value, code];
    }
    notify(`已切分 ${result.count} 段（新增 ${result.inserted} / 复用 ${result.reused}），点「保存勾选」生效`);
    resetAddForm();
    showAdd.value = false;
    open.value = true;
  } catch (e) {
    void alertDialog("导入知识库失败", e instanceof Error ? e.message : String(e));
  } finally {
    adding.value = false;
  }
};

watch(
  () => props.interviewerCode,
  (code) => {
    if (code) void load(code);
  },
  { immediate: true },
);
</script>
