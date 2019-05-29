package io.choerodon.kb.infra.common.utils.diff;

import java.util.*;
import java.util.stream.Collectors;

import difflib.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.choerodon.kb.api.dao.TextDiffDTO;

/**
 * diffUtil，用于生成两篇文字的差异内容TextDiffDTO
 *
 * @author shinan.chen
 * @since 2019/5/16
 */
public class DiffUtil {

    private DiffUtil() {

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DiffUtil.class);

    /**
     * 生成两个字符串的差异内容TextDiffDTO
     *
     * @param original
     * @param revised
     * @return
     */
    public static TextDiffDTO diff(String original, String revised) {
        final List<Delta<String>> deltas = getDeltas(original, revised);
        return deltas2Dto(deltas);
    }

    /**
     * 正序解析diff列表为目标文本
     *
     * @param diffs
     * @return
     */
    public static String parseObverse(List<TextDiffDTO> diffs) {
        String target = "";
        for (TextDiffDTO diff : diffs) {
            target = applyTo(diff, target);
        }
        return target;
    }

    /**
     * 倒序解析diff列表为目标文本
     *
     * @param diffs
     * @param latestContent
     * @return
     */
    public static String parseReverse(List<TextDiffDTO> diffs, String latestContent) {
        Collections.reverse(diffs);
        for (TextDiffDTO diff : diffs) {
            latestContent = restore(diff, latestContent);
        }
        return latestContent;
    }

    /**
     * 应用一个diff到文本中
     *
     * @param diff
     * @param original
     * @return
     */
    private static String applyTo(TextDiffDTO diff, String original) {
        final List<String> originalFileLines = textToLines(original);
        List<String> result = new LinkedList<>(originalFileLines);
        List<Delta<String>> deltas = dto2Deltas(diff);
        Collections.sort(deltas, DeltaComparator.INSTANCE);
        ListIterator<Delta<String>> it = deltas.listIterator(deltas.size());
        try {
            while (it.hasPrevious()) {
                Delta<String> delta = it.previous();
                delta.applyTo(result);
            }
        } catch (PatchFailedException e) {
            LOGGER.error(e.getMessage());
        }
        return linesToText(result);
    }

    /**
     * 反向应用一个diff到文本中
     *
     * @param diff
     * @param revised
     * @return
     */
    private static String restore(TextDiffDTO diff, String revised) {
        final List<String> revisedFileLines = textToLines(revised);
        List<String> result = new LinkedList<>(revisedFileLines);
        List<Delta<String>> deltas = dto2Deltas(diff);
        Collections.sort(deltas, DeltaComparator.INSTANCE);
        ListIterator<Delta<String>> it = deltas.listIterator(deltas.size());
        while (it.hasPrevious()) {
            Delta<String> delta = it.previous();
            delta.restore(result);
        }
        return linesToText(result);
    }

    /**
     * 根据DiffUtils.diff()获取差异Delta信息
     *
     * @param original
     * @param revised
     * @return
     */
    private static List<Delta<String>> getDeltas(String original, String revised) {

        final List<String> originalFileLines = textToLines(original);
        final List<String> revisedFileLines = textToLines(revised);
        final Patch patch = DiffUtils.diff(originalFileLines, revisedFileLines);
        return patch.getDeltas();
    }

    /**
     * Delta信息转差异内容TextDiffDTO
     *
     * @param deltas
     * @return
     */
    private static TextDiffDTO deltas2Dto(List<Delta<String>> deltas) {
        final List<Delta<String>> insert = new ArrayList<>();
        final List<Delta<String>> delete = new ArrayList<>();
        final List<Delta<String>> change = new ArrayList<>();
        for (Delta delta : deltas) {
            if (delta.getType() == Delta.TYPE.INSERT) {
                insert.add(delta);
            } else if (delta.getType() == Delta.TYPE.DELETE) {
                delete.add(delta);
            } else {
                change.add(delta);
            }
        }
        return new TextDiffDTO(insert, delete, change);
    }

    /**
     * 差异内容TextDiffDTO转Delta信息
     *
     * @param dto
     * @return
     */
    private static List<Delta<String>> dto2Deltas(TextDiffDTO dto) {
        List<Delta<String>> deltas = new ArrayList<>(dto.getChangeData().size() + dto.getInsertData().size() + dto.getDeleteData().size());
        deltas.addAll(dto.getInsertData());
        deltas.addAll(dto.getDeleteData());
        deltas.addAll(dto.getChangeData());
        return deltas;
    }

    public static List<String> textToLines(String text) {
        return Arrays.asList(text.split("\\n"));
    }

    private static String linesToText(List<String> lines) {
        return lines.stream().collect(Collectors.joining("\n"));
    }
}
