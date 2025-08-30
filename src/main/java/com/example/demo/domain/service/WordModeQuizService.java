// service/WordModeQuizService.java
package com.example.demo.domain.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.domain.entity.Word;
import com.example.demo.domain.repository.WordRepository;
import com.example.demo.dto.QuizBatchResponse;
import com.example.demo.dto.QuizOption;
import com.example.demo.dto.QuizQuestion;

@Service
public class WordModeQuizService {
    private final WordRepository repo;
    private final Random rnd = new Random();

    public WordModeQuizService(WordRepository repo){ this.repo = repo; }

    public QuizBatchResponse generate(int count){
        // 1) プール取得（全体＋副詞）
        List<Word> all = new ArrayList<>(repo.findTop2000ByOrderByIdAsc());
        if(all.isEmpty()){
            return emptyBatch(); // ← ここで空返却してもフロントが落ちない
        }
        Collections.shuffle(all, rnd);

        List<Word> adverbs = all.stream()
                .filter(w -> "adverb".equalsIgnoreCase(n(w.getPart())) && notEmpty(w.getMeaning()))
                .collect(Collectors.toList());

        List<Word> actions = all.stream()
                .filter(w -> !"adverb".equalsIgnoreCase(n(w.getPart()))
                        && (notEmpty(w.getPictUrlStatic()) || notEmpty(w.getPictDescription())))
                .collect(Collectors.toList());

        List<QuizQuestion> out = new ArrayList<>();
        int guard = 0;
        while(out.size() < count && guard++ < 1000){
            boolean pickAdverb = (out.size() % 2 == 0); // 半々で混ぜる
            if(pickAdverb && adverbs.size() >= 4){
                Word ans = pickOne(adverbs);
                List<Word> distract = pickDistinct(adverbs, ans, 3);
                out.add(buildAdverb(ans, distract));
            }else if(!pickAdverb && actions.size() >= 4){
                Word ans = pickOne(actions);
                List<Word> distract = pickDistinct(actions, ans, 3);
                out.add(buildAction(ans, distract));
            }else{
                // どちらかが不足なら、ある方で補充
                if(actions.size() >= 4){
                    Word ans = pickOne(actions);
                    List<Word> distract = pickDistinct(actions, ans, 3);
                    out.add(buildAction(ans, distract));
                }else if(adverbs.size() >= 4){
                    Word ans = pickOne(adverbs);
                    List<Word> distract = pickDistinct(adverbs, ans, 3);
                    out.add(buildAdverb(ans, distract));
                }else{
                    break;
                }
            }
        }

        QuizBatchResponse resp = new QuizBatchResponse();
        resp.questions = out.size() > count ? out.subList(0, count) : out;
        return resp;
    }

    // ===== helper =====
    private QuizBatchResponse emptyBatch(){
        QuizBatchResponse r = new QuizBatchResponse();
        r.questions = Collections.emptyList();
        return r;
    }
    private Word pickOne(List<Word> list){ return list.get(rnd.nextInt(list.size())); }

    private List<Word> pickDistinct(List<Word> pool, Word exclude, int k){
        List<Word> c = pool.stream().filter(w -> !Objects.equals(w.getId(), exclude.getId()))
                .collect(Collectors.toList());
        Collections.shuffle(c, rnd);
        return c.stream().limit(k).collect(Collectors.toList());
    }

    private QuizQuestion buildAction(Word ans, List<Word> ds){
        List<QuizOption> opts = new ArrayList<>();
        opts.add(toActionOpt(ans, true));
        ds.forEach(d -> opts.add(toActionOpt(d, false)));
        Collections.shuffle(opts, rnd);

        QuizQuestion q = new QuizQuestion();
        q.id = String.valueOf(ans.getId());
        q.questionType = "action";
        q.prompt = n(ans.getWord());
        q.word = n(ans.getWord());
        q.part = n(ans.getPart());
        q.options = opts;
        return q;
    }
    private QuizOption toActionOpt(Word w, boolean correct){
        QuizOption o = new QuizOption();
        o.label = n(w.getPictDescription());
        o.imageUrl = n(w.getPictUrlStatic());
        o.correct = correct;
        return o;
    }

    private QuizQuestion buildAdverb(Word ans, List<Word> ds){
        List<QuizOption> opts = new ArrayList<>();
        opts.add(toTextOpt(n(ans.getWord()), true));
        ds.forEach(d -> opts.add(toTextOpt(n(d.getWord()), false)));
        Collections.shuffle(opts, rnd);

        QuizQuestion q = new QuizQuestion();
        q.id = String.valueOf(ans.getId());
        q.questionType = "adverb";
        q.prompt = n(ans.getMeaning()); // 日本語表示
        q.word = n(ans.getWord());
        q.part = "adverb";
        q.options = opts;
        return q;
    }
    private QuizOption toTextOpt(String text, boolean correct){
        QuizOption o = new QuizOption();
        o.label = text;
        o.imageUrl = null;
        o.correct = correct;
        return o;
    }
    private static boolean notEmpty(String s){ return s != null && !s.isBlank(); }
    private static String n(String s){ return s == null ? "" : s; }
    
}
