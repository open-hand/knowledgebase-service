package io.choerodon.kb.infra.common.utils;

import difflib.*;
import io.choerodon.kb.api.dao.TextDiffDTO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author shinan.chen
 * @since 2019/5/16
 */
public class DiffUtil {

    public static TextDiffDTO diff(String original, String revised) {
        final List<Delta<String>> deltas = getDeltas(original, revised);
        return deltas2Dto(deltas);
    }

    public static String applyTo(TextDiffDTO diff, String original) {
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
            e.printStackTrace();
        }
        return linesToText(result);
    }

    public static String restore(TextDiffDTO diff, String revised) {
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

    private static List<Delta<String>> getDeltas(String original, String revised) {

        final List<String> originalFileLines = textToLines(original);
        final List<String> revisedFileLines = textToLines(revised);
        final Patch patch = DiffUtils.diff(originalFileLines, revisedFileLines);
        return patch.getDeltas();
    }

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

    private static List<Delta<String>> dto2Deltas(TextDiffDTO dto) {
        List<Delta<String>> deltas = new ArrayList<>(dto.getChangeData().size() + dto.getInsertData().size() + dto.getDeleteData().size());
        deltas.addAll(dto.getInsertData());
        deltas.addAll(dto.getDeleteData());
        deltas.addAll(dto.getChangeData());
        return deltas;
    }

    private static List<String> textToLines(String text) {
        return Arrays.asList(text.split("\\n"));
    }

    private static String linesToText(List<String> lines) {
        return lines.stream().collect(Collectors.joining("\n"));
    }
}
