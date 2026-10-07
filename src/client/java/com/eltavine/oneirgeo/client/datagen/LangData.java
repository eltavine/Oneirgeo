package com.eltavine.oneirgeo.client.datagen;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

/** English and Simplified Chinese strings, kept side by side so they never drift apart. */
final class LangData {
    private static final Map<String, String[]> STRINGS = new LinkedHashMap<>();

    private LangData() {
    }

    static void put(String key, String english, String chinese) {
        STRINGS.put(key, new String[]{english, chinese});
    }

    static {
        put("itemGroup.oneirgeo", "Oneirgeo", "梦域");

        put("block.oneirgeo.monolith_stone", "Monolith Stone", "巨碑石");
        put("block.oneirgeo.cloud", "Cloud", "云");
        put("block.oneirgeo.temple_marble", "Temple Marble", "神殿大理石");
        put("block.oneirgeo.faded_plaster", "Faded Plaster", "褪色灰泥");
        put("block.oneirgeo.pool_tile", "Pool Tile", "泳池瓷砖");
        put("block.oneirgeo.pool_tile_blue", "Blue Pool Tile", "蓝色泳池瓷砖");
        put("block.oneirgeo.pool_light", "Pool Light", "泳池灯");
        put("block.oneirgeo.wallpaper", "Yellowed Wallpaper", "泛黄墙纸");
        put("block.oneirgeo.damp_carpet", "Damp Carpet", "潮湿地毯");
        put("block.oneirgeo.ceiling_tile", "Ceiling Tile", "吊顶板");
        put("block.oneirgeo.fluorescent_light", "Fluorescent Light", "荧光灯");
        put("block.oneirgeo.ash", "Ash", "灰烬");
        put("block.oneirgeo.cold_lava", "Cold Lava", "冷却熔岩");
        put("block.oneirgeo.boiler_plate", "Boiler Plate", "锅炉钢板");
        put("block.oneirgeo.ember", "Ember", "余烬");
        put("block.oneirgeo.star_stone", "Star Stone", "星石");
        put("block.oneirgeo.grave_stone", "Grave Stone", "墓石");
        put("block.oneirgeo.night_plaster", "Night Plaster", "夜色灰泥");
        put("block.oneirgeo.mirror_surface", "Mirror Surface", "镜面");
        put("block.oneirgeo.updraft", "Updraft", "上升气流");
        put("block.oneirgeo.updraft_vent", "Updraft Vent", "上升气流口");
        put("block.oneirgeo.elevator", "Elevator Block", "电梯方块");
        put("block.oneirgeo.synapse", "Synapse", "突触");
        put("block.oneirgeo.tissue_eye", "Eye", "眼睛");
        put("block.oneirgeo.mirror_frame", "Mirror Frame", "镜框");
        put("block.oneirgeo.mirror_portal", "Mirror", "镜子");
        put("block.oneirgeo.backrooms_door", "Door That Should Not Be Here", "不该在这里的门");
        put("block.oneirgeo.exit_door", "Exit", "出口");
        put("block.oneirgeo.wake_door", "Door to Waking", "通往醒来的门");
        put("block.oneirgeo.closed_door", "Door", "门");
        put("oneirgeo.door.closed", "It does not open. It never did.", "它打不开。从来都打不开。");
        put("oneirgeo.sign.way_back", "the way back", "回去的路");
        put("oneirgeo.trap.no_spawn", "Nothing here will remember you", "这里什么都不会记得你");
        put("oneirgeo.supply.arrived", "There is something in your pocket that was not there before", "口袋里多了些原本没有的东西");
        put("oneirgeo.supply.none", "Nothing reaches you here", "这里收不到任何东西");
        put("item.oneirgeo.lucid_tea", "Lucid Tea", "清醒茶");
        put("item.oneirgeo.mirror_shard", "Mirror Shard", "镜子碎片");
        put("item.oneirgeo.memory_fragment", "Memory Fragment", "记忆碎片");
        put("entity.oneirgeo.faceless", "Faceless", "无脸者");
        put("entity.oneirgeo.stalker", "Stalker", "追踪者");
        put("entity.oneirgeo.mimic", "Mimic", "模仿者");
        put("entity.oneirgeo.lifeguard", "Lifeguard", "救生员");
        put("entity.oneirgeo.nurse", "Night Nurse", "夜班护士");
        put("oneirgeo.mirror.dive", "You sink through your own reflection.", "你沉进了自己的倒影里。");
        put("oneirgeo.mirror.blocked", "Something is standing in the way on the other side of the glass.", "镜子另一边有什么东西挡着。");
        put("oneirgeo.mirror.surface", "You come up through the glass. Nobody is waiting on the other side.", "你穿过镜面浮了上来。另一边没有人在等你。");
        put("oneirgeo.lifeguard.rescue", "A whistle. Someone pulls you out of the deep end.", "一声哨响。有人把你从深水区拉了上来。");
        put("oneirgeo.nurse.bed", "Someone takes your hand and leads you back to bed.", "有人牵起你的手，把你带回床上。");
        put("subtitles.oneirgeo.lights_out", "The lights go out", "灯灭了");
        String[][] fragments = {
                {"the house had a red carpet. I remember the carpet.", "那栋房子有一块红地毯。我记得那块地毯。"},
                {"we used to watch the telephone lines at dusk", "我们以前常在黄昏看电线"},
                {"someone was calling my name from the next room", "有人在隔壁房间叫我的名字"},
                {"the pool closed in September. nobody told the water.", "泳池九月就关了。没人告诉那些水。"},
                {"I counted the doors twice. there were more the second time.", "我把门数了两遍。第二遍更多。"},
                {"the stars were wrong that night too", "那天晚上星星也不对"},
                {"my hands were smaller when I last stood here", "上次站在这里时，我的手还更小"},
                {"if you are reading this, you fell asleep again", "如果你在读这个，说明你又睡着了"},
                {"the elevator only went down", "电梯只会往下"},
                {"I left the light on so you could find the way back", "我留着灯，好让你找到回去的路"},
                {"the doctor said it was only a fever", "医生说那只是发烧"},
                {"there is a door in every dream. it is always the same door.", "每个梦里都有一扇门。总是同一扇门。"},
                {"the sea was so still I could see myself walking upside down", "海面静得能看见自己倒着走"},
                {"we buried something under the observatory. I forget what.", "我们在天文台下面埋了什么。我忘了是什么。"},
                {"the hum never stops. you stop hearing it.", "嗡嗡声从不停止。只是你不再听见。"},
                {"mother's voice, but slower", "妈妈的声音，只是慢了一些"},
                {"I tried to wake up and woke up here", "我想醒来，醒来就在这里"},
                {"all the clocks in the house stopped at the same minute", "家里所有的钟都停在同一分钟"},
                {"it follows the way you came, not the way you go", "它跟着你来的路，而不是你去的路"},
                {"the tissue remembers being a thought", "这些组织记得自己曾是一个念头"},
                {"don't look at the tall one for too long. don't look away either.", "别盯着那个高的太久。也别移开视线。"},
                {"the barrels in the dark rooms are not all barrels", "暗室里的桶，并不都是桶"},
                {"I wrote this so I would remember. I don't.", "我写下这些是为了记住。我没有记住。"},
                {"there is someone standing where the fog thins", "雾薄的地方站着一个人"},
        };
        for (int i = 0; i < fragments.length; i++) {
            put("oneirgeo.fragment." + i, fragments[i][0], fragments[i][1]);
        }
        put("item.oneirgeo.dream_journal", "Dream Journal", "梦境日记");
        put("item.oneirgeo.vhs_tape", "VHS Tape", "录像带");
        put("oneirgeo.journal.found", "There is a notebook in your pocket. A few of its pages are in your handwriting.", "口袋里有一本笔记本。有几页是你的笔迹。");
        put("oneirgeo.journal.title", "Dream Journal", "梦境日记");
        put("oneirgeo.journal.intro", "Things I remembered, in the order they came back.", "我想起来的事，按想起来的顺序记下。");
        put("oneirgeo.journal.contents", "%s  %s/%s", "%s  %s/%s");
        put("oneirgeo.journal.last.0", "It is still 3:17. It has been 3:17 for %s years.", "现在还是三点十七分。已经三点十七分%s年了。");
        put("oneirgeo.journal.last.1", "Every whisper was them. Every \"wake up\" was them. The light is still on.", "每一句低语都是他们。每一句「醒醒」都是他们。灯还亮着。");
        put("oneirgeo.journal.last.2", "The stairs under the house go further down now.", "房子下面的楼梯，现在能走得更深了。");
        put("oneirgeo.fragment.of", "A memory of %s", "关于「%s」的记忆");
        put("oneirgeo.fragment.known", "You already remember everything about %s.", "关于「%s」的事，你已经全都想起来了。");
        put("oneirgeo.story.complete", "You remember %s. There is a tape in your pocket.", "你想起了「%s」。口袋里多了一盘录像带。");
        put("oneirgeo.story.all", "You remember everything. The stairs under the house go further down now.", "你全都想起来了。房子下面的楼梯，现在能走得更深了。");
        put("oneirgeo.story.final", "The room where the clocks stopped. The light is still on.", "钟停下的房间。灯还亮着。");
        put("oneirgeo.tape.no_player", "There is nothing here to play it on.", "这里没有能放它的东西。");
        put("oneirgeo.belonging.kept", "You cannot bring yourself to let go of it.", "你舍不得把它丢下。");
        String[][] chapters = {
                {"house", "The House", "家"},
                {"fever", "The Fever", "高烧"},
                {"observatory", "The Observatory", "天文台"},
                {"reflection", "The Reflection", "倒影"},
                {"pool", "The September Pool", "九月的泳池"},
                {"waiting", "The Waiting Room", "候诊室"},
        };
        String[][][] entries = {
                {
                        {"The house had a red carpet on the stairs. I counted the steps every night: fourteen. There are more now.",
                                "楼梯上铺着红地毯。我每天晚上都数台阶：十四级。现在不止十四级了。"},
                        {"At dusk the telephone lines hummed. Dad said it was people talking far away. I think it was the wires remembering.",
                                "黄昏时电线会嗡嗡响。爸爸说那是远方的人在说话。我觉得是电线在回忆。"},
                        {"Mom called me from the kitchen. I said \"coming\". I am still on my way.",
                                "妈妈在厨房叫我。我说「来了」。我现在还在路上。"},
                        {"My room was at the end of the hall. The door stuck in summer; I learned to open it with my shoulder.",
                                "我的房间在走廊尽头。夏天门会卡住，我学会了用肩膀把它顶开。"},
                        {"There was a crack in my ceiling shaped like a river. I followed it with my eyes every night until I fell asleep. Here every crack is a river.",
                                "我房间的天花板上有一道裂缝，像一条河。我每晚都用眼睛顺着它走，直到睡着。这里的每一道裂缝都是一条河。"},
                        {"Dad filmed everything with his camcorder. \"For later,\" he said. I didn't know later meant this.",
                                "爸爸什么都用摄像机拍下来。他说「留着以后看」。那时我不知道「以后」说的是现在。"},
                        {"The last night in the house I had a headache. Mom put a cold towel on my forehead and turned off the light. I asked her to leave it on.",
                                "在家的最后一晚，我头很疼。妈妈把凉毛巾敷在我额头上，关了灯。我求她别关。"},
                },
                {
                        {"The fever came the next morning. Everything was too bright and too far away.",
                                "第二天早上，烧起来了。所有东西都太亮，又太远。"},
                        {"Forty-one point two. I heard the number and saw it burning.",
                                "四十一点二度。我听见了这个数字，也看见它在烧。"},
                        {"The doctor said it was only a fever. He said it twice. The second time he said it to mom.",
                                "医生说只是发烧。他说了两遍，第二遍是对妈妈说的。"},
                        {"They put me in a bath of ice. I dreamed of furnaces that had gone out, and I was the last warm thing in them.",
                                "他们把我放进冰水里。我梦见熄灭的熔炉，而我是里面最后一样还热着的东西。"},
                        {"The ceiling was upside down, or I was. A whole city hung from it and nobody fell.",
                                "天花板是倒过来的，也可能是我倒过来了。上面挂着一整座城，谁也没有掉下来。"},
                        {"Mom's hand was cool on my face. Then it was gone, and the room was very loud.",
                                "妈妈的手贴在我脸上，凉凉的。后来那只手不见了，房间变得很吵。"},
                        {"Somebody said \"seizure\". At 3:17 every clock in the house stopped, even the ones that were far away.",
                                "有人说了「抽搐」。三点十七分，家里所有的钟都停了，连很远的那些也停了。"},
                },
                {
                        {"Before the fever, dad took me to the observatory on the hill. It was the best night of the summer.",
                                "发烧之前，爸爸带我去了山上的天文台。那是那年夏天最好的一个晚上。"},
                        {"He showed me how to find the pole star. \"If you're ever lost, it stays where it is.\"",
                                "他教我找北极星。「要是你迷路了，它会一直待在原地。」"},
                        {"We counted the moons of Jupiter. I counted one more than there were. He didn't correct me.",
                                "我们数木星的卫星。我多数了一颗。他没有纠正我。"},
                        {"We buried a tin box under the observatory floor, right below the telescope: a drawing, a tape and a marble. \"Open it when you're grown up.\"",
                                "我们在天文台的地板下面，就在望远镜正下方，埋了一个铁皮盒：一张画，一盘录像带，一颗玻璃弹珠。「等你长大了再打开。」"},
                        {"Dad wrote the numbers on the back of the star map so we could find it again: %s, %s.",
                                "爸爸把数字写在星图背面，好让我们以后找得到：%s，%s。"},
                        {"Now the stars are wrong. There is an eye where the moon should be. I think it is looking for me too.",
                                "现在星星都不对了。月亮该在的地方是一只眼睛。我觉得它也在找我。"},
                        {"Every night the sky turns back a little. Maybe it is trying to get back to that night.",
                                "每天晚上，天空都往回倒一点。也许它想回到那个晚上。"},
                },
                {
                        {"The sea is so still it shows me upside down. My reflection is taller than me.",
                                "海面静得能把我倒过来照出来。倒影比我高。"},
                        {"My reflection's hands are bigger than mine. It has a scar I don't have yet.",
                                "倒影的手比我的大。它身上有一道我还没有的伤疤。"},
                        {"It moves a moment after I do. Or I move a moment after it.",
                                "它总是比我慢一点。也可能是我比它慢一点。"},
                        {"Mirrors don't show the room. They show the room as it will be when nobody is in it.",
                                "镜子照出来的不是房间，是房间以后没有人时的样子。"},
                        {"I asked my reflection how old I am. It counted on its fingers and ran out of fingers.",
                                "我问倒影我几岁了。它掰着手指数，手指不够用了。"},
                        {"Under the surface there is another world, the same as this one, where I am awake.",
                                "水面下面还有一个世界，和这里一模一样，那边的我是醒着的。"},
                        {"If I go down there, it comes up here. We can't both be awake.",
                                "如果我下去，它就会上来。我们不能都醒着。"},
                },
                {
                        {"On the last day of summer we went to the public pool. The water was as warm as a bath.",
                                "夏天的最后一天，我们去了公共泳池。水暖得像洗澡水。"},
                        {"I held my breath and stayed at the bottom as long as I could. The lifeguard blew his whistle.",
                                "我憋着气，在池底待得尽量久。救生员吹响了哨子。"},
                        {"Water went up my nose. It burned. I laughed and did it again.",
                                "水呛进了鼻子，火辣辣的。我笑着又来了一次。"},
                        {"That night my head hurt behind my eyes, as if the water was still in there.",
                                "那天晚上，我眼睛后面疼，好像水还留在里面。"},
                        {"The pool closed in September. Nobody told the water. It is still waiting for us to come back.",
                                "泳池九月就关了。没人告诉那些水。它还在等我们回去。"},
                        {"Someone asked mom whether I had been swimming. She cried for a long time after that.",
                                "有人问妈妈我是不是去游过泳。那之后她哭了很久。"},
                        {"Every pool here is that pool. Every lifeguard is that lifeguard. He is still watching the deep end.",
                                "这里的每一个泳池都是那个泳池。每一个救生员都是那个救生员。他还在盯着深水区。"},
                },
                {
                        {"The hospital corridor had lights that hummed like the telephone lines. Mom and dad sat under them for a long time.",
                                "医院走廊里的灯，像电线一样嗡嗡响。爸爸妈妈在灯下坐了很久。"},
                        {"The vending machine had my favourite drink. Dad bought one every day and never drank it.",
                                "自动售货机里有我最爱喝的饮料。爸爸每天买一瓶，一口也不喝。"},
                        {"The nurse said I could hear them. So they talked: about the house, the stars, the pool.",
                                "护士说我听得见。于是他们就说话，说家，说星星，说泳池。"},
                        {"Days stopped having names. The yellow walls stayed the same colour for years.",
                                "日子不再有名字。黄色的墙壁好多年都是同一个颜色。"},
                        {"They asked mom whether to keep the light on. She said yes. She says yes every time.",
                                "他们问妈妈要不要一直开着灯。她说要。每一次她都说要。"},
                        {"Dad reads to me now. His voice comes through the walls, slower than it should be.",
                                "现在爸爸给我念书。他的声音穿过墙壁传过来，比原本慢一些。"},
                        {"There is a door at the end of every corridor. It is always the same door. On the other side, someone is holding my hand.",
                                "每条走廊尽头都有一扇门。总是同一扇门。门的另一边，有人握着我的手。"},
                },
        };
        for (int c = 0; c < chapters.length; c++) {
            put("oneirgeo.story." + chapters[c][0], chapters[c][1], chapters[c][2]);
            for (int e = 0; e < entries[c].length; e++) {
                put("oneirgeo.story." + chapters[c][0] + "." + e, entries[c][e][0], entries[c][e][1]);
            }
        }
        String[][][] tapes = {
                {{"house"}, {"AUG. 30 1997", "AUG. 30 1997"}, {"HOME - DON'T TAPE OVER", "家 —— 别覆盖"},
                        {"[a hallway. a red carpet runs up the stairs]", "[一条走廊。红地毯一直铺到楼梯上]"},
                        {"DAD: say hello to later!", "爸爸：跟以后的你打个招呼！"},
                        {"[you wave. you are missing a front tooth]", "[你挥了挥手。你缺了一颗门牙]"},
                        {"MOM (from the kitchen): dinner's ready - come down, both of you", "妈妈（在厨房）：吃饭了——你们俩都下来"},
                        {"[the camera turns to the window. the sun is setting. it keeps setting]", "[镜头转向窗外。太阳正在落下。一直在落下]"}},
                {{"fever"}, {"SEP. 02 1997", "SEP. 02 1997"}, {"(no label)", "（没有标签）"},
                        {"[no picture. the lens cap is still on]", "[没有画面。镜头盖还没摘]"},
                        {"MOM: forty-one point two. call him again.", "妈妈：四十一点二度。再给他打一次电话。"},
                        {"DAD: the line's busy. it's been busy for an hour.", "爸爸：占线。已经占线一个小时了。"},
                        {"[a cold cloth being wrung out. water dripping into a bowl]", "[拧凉毛巾的声音。水滴进盆里]"},
                        {"YOU: is the house on fire? it's so hot in here", "你：房子着火了吗？这里好热"},
                        {"[the recording stops at 3:17]", "[录像停在三点十七分]"}},
                {{"observatory"}, {"AUG. 24 1997", "AUG. 24 1997"}, {"STARS W/ KIDDO", "和孩子看星星"},
                        {"[the dome of the observatory opens. stars, too many to count]", "[天文台的圆顶打开了。星星多得数不清]"},
                        {"DAD: that one's the pole star. it stays where it is.", "爸爸：那颗是北极星。它会一直待在原地。"},
                        {"YOU: what if I get lost somewhere it can't see?", "你：要是我迷路到它看不见的地方呢？"},
                        {"DAD: then I'll come and find you. I'll bring the camera.", "爸爸：那我就去找你。我会带着摄像机。"},
                        {"[you both laugh. the tape runs out]", "[你们俩都笑了。录像带到头了]"}},
                {{"reflection"}, {"--- -- ----", "--- -- ----"}, {"(blank)", "（空白）"},
                        {"[a mirror. you in it, older than you are]", "[一面镜子。镜子里的你，比你现在大]"},
                        {"[the reflection lifts its hand a moment after you do]", "[你抬起手，过了一会儿倒影才抬手]"},
                        {"[it mouths something. it might be your name. it might be its own]", "[它的嘴动了动。可能是你的名字，也可能是它自己的]"},
                        {"[it turns around and walks deeper into the glass]", "[它转过身，往镜子更深处走去]"}},
                {{"pool"}, {"AUG. 31 1997", "AUG. 31 1997"}, {"LAST SWIM OF SUMMER", "夏天最后一次游泳"},
                        {"[a public pool, blue and loud. a whistle somewhere]", "[一个公共泳池，蓝色的，很吵。某处有哨声]"},
                        {"DAD: one more dive and then we're going home", "爸爸：再跳一次，然后我们就回家"},
                        {"[you jump. the camera follows the splash, and waits]", "[你跳了下去。镜头追着水花，然后等着]"},
                        {"[you come up laughing. you rub your forehead]", "[你笑着浮上来。你揉了揉额头]"},
                        {"LIFEGUARD: the pool's closing for the season, folks", "救生员：泳池这一季要关了，各位"}},
                {{"waiting"}, {"SEP. 03 1997", "SEP. 03 1997"}, {"HOSPITAL", "医院"},
                        {"[a corridor. yellow walls. fluorescent lights humming]", "[一条走廊。黄色的墙。荧光灯嗡嗡作响]"},
                        {"[mom asleep in a plastic chair, holding a cold cup of tea]", "[妈妈靠在塑料椅子上睡着了，手里攥着一杯凉掉的茶]"},
                        {"DAD (quietly): the nurse says you can hear us.", "爸爸（小声）：护士说你听得见我们。"},
                        {"DAD: so we'll keep talking. we'll keep the light on.", "爸爸：所以我们会一直说话，会一直开着灯。"},
                        {"[the date in the corner keeps changing. the corridor doesn't]", "[角落里的日期一直在变。走廊一直没变]"}},
                {{"capsule"}, {"AUG. 24 1997", "AUG. 24 1997"}, {"OPEN WHEN YOU'RE GROWN UP", "等你长大了再打开"},
                        {"[dad's face, too close to the lens]", "[爸爸的脸，离镜头太近了]"},
                        {"DAD: hi. if you're watching this, you're grown up now.", "爸爸：嗨。如果你在看这个，说明你已经长大了。"},
                        {"DAD: I hope it was a good life. I hope we were there for all of it.", "爸爸：希望你过得很好。希望我们一直都在。"},
                        {"DAD: if you ever get lost, count the stairs at home. there were always fourteen.", "爸爸：要是你哪天迷路了，就数一数家里的楼梯。一直都是十四级。"},
                        {"[he looks away from the camera for a long time]", "[他移开视线，很久没有看镜头]"}},
                {{"glitch"}, {"SEP. 14 1997", "SEP. 14 1997"}, {"", ""},
                        {"[you, asleep, in a room you don't remember]", "[你在一个记不起来的房间里睡着]"},
                        {"[someone off camera: is it still recording?]", "[镜头外有人说：还在录吗？]"}},
                {{"final"}, {"SEP. 03 1997", "SEP. 03 1997"}, {"", ""},
                        {"[a hospital room. a bed. a machine counts something, slowly]", "[一间病房。一张床。一台机器在慢慢地数着什么]"},
                        {"[on the wall, a clock that says 3:17. it has said 3:17 for a long time]", "[墙上的钟指着三点十七分。它已经指着三点十七分很久了]"},
                        {"[the camcorder is on a tripod by the window. its red light is on]", "[摄像机架在窗边的三脚架上。红灯亮着]"},
                        {"MOM: we're still here.", "妈妈：我们还在这里。"},
                        {"DAD: whenever you're ready. there's no hurry.", "爸爸：等你准备好。不着急。"},
                        {"[the light stays on]", "[灯一直亮着]"}},
        };
        for (String[][] tape : tapes) {
            String id = tape[0][0];
            put("oneirgeo.tape." + id + ".date", tape[1][0], tape[1][1]);
            put("oneirgeo.tape." + id + ".label", tape[2][0], tape[2][1]);
            for (int line = 3; line < tape.length; line++) {
                put("oneirgeo.tape." + id + "." + (line - 3), tape[line][0], tape[line][1]);
            }
        }
        put("block.oneirgeo.steam_vent", "Steam Vent", "蒸汽喷口");
        put("block.oneirgeo.television", "CRT Television", "显像管电视");
        put("block.oneirgeo.telephone", "Telephone", "电话");
        put("block.oneirgeo.stopped_clock", "Stopped Clock", "停摆的钟");
        put("block.oneirgeo.hospital_bed", "Hospital Bed", "病床");
        put("block.oneirgeo.iv_stand", "IV Stand", "输液架");
        put("block.oneirgeo.heart_monitor", "Heart Monitor", "心电监护仪");
        put("block.oneirgeo.waiting_chair", "Waiting Room Chair", "候诊椅");
        put("block.oneirgeo.telescope", "Telescope", "望远镜");
        put("item.oneirgeo.glass_marble", "Glass Marble", "玻璃弹珠");
        put("item.oneirgeo.child_drawing", "Child's Drawing", "儿童画");
        put("oneirgeo.clock.stopped", "It is 3:17. It is always 3:17.", "三点十七分。永远是三点十七分。");
        put("oneirgeo.bed.hospital", "The sheets are cold. Someone was here a moment ago.", "床单是凉的。刚才还有人在这里。");
        put("oneirgeo.iv.use", "The bag is almost empty. Someone keeps refilling it.", "输液袋快空了。总有人来把它续上。");
        put("oneirgeo.chair.waiting", "The seat is still warm.", "椅子上还有余温。");
        put("oneirgeo.monitor.use", "The line on the screen climbs, falls, and climbs again.", "屏幕上的线升起，落下，又升起。");
        put("oneirgeo.telescope.star", "One star burns brighter than the rest, low over the %s, about %s blocks away.", "有一颗星比其他的都亮，低低地挂在%s方，大约%s格远。");
        put("oneirgeo.telescope.here", "The brightest star is right overhead. Whatever it marks is under your feet.", "最亮的那颗星就在头顶。它标记的东西就在你脚下。");
        put("oneirgeo.telescope.nothing", "Only fog, and the shapes of things too far away to name.", "只有雾，还有远得叫不出名字的东西的轮廓。");
        String[][] directions = {{"north", "north", "北"}, {"north_east", "north-east", "东北"}, {"east", "east", "东"}, {"south_east", "south-east", "东南"},
                {"south", "south", "南"}, {"south_west", "south-west", "西南"}, {"west", "west", "西"}, {"north_west", "north-west", "西北"}};
        for (String[] d : directions) {
            put("oneirgeo.direction." + d[0], d[1], d[2]);
        }
        put("oneirgeo.marble.use", "It is cold, and there is a tiny house inside it.", "它凉凉的，里面有一座小小的房子。");
        put("oneirgeo.drawing.use", "A house, three people and a sun going down. The smallest person is coloured in black.", "一座房子，三个人，一个正在落下的太阳。最小的那个人被涂成了黑色。");
        put("oneirgeo.phone.tone", "A dial tone. It sounds a long way off.", "拨号音。听起来很远很远。");
        String[][] phone = {
                {"...hello? sweetheart? can you hear me?", "……喂？宝贝？你听得见吗？"},
                {"the doctor says you can hear us. so I'll keep talking.", "医生说你听得见我们。那我就一直说。"},
                {"we brought your star map. it's on the table by the window.", "我们把你的星图带来了。放在窗边的桌上。"},
                {"it's raining here. you always liked the rain.", "这里在下雨。你一直喜欢下雨。"},
                {"your room is just the way you left it.", "你的房间还是你走时的样子。"},
                {"[breathing. a machine counting, slowly]", "[呼吸声。一台机器在慢慢地数着]"},
                {"[a dial tone, then your own voice, much younger: \"coming!\"]", "[拨号音，然后是你自己的声音，小很多：「来了！」]"},
                {"dad says he'll bring the camcorder tomorrow.", "爸爸说他明天会把摄像机带来。"},
                {"it's 3:17 again. it's always 3:17 when I call.", "又是三点十七分。我打来的时候总是三点十七分。"},
                {"please.", "求你了。"},
        };
        for (int i = 0; i < phone.length; i++) {
            put("oneirgeo.phone." + i, phone[i][0], phone[i][1]);
        }
        String[][] snow = {
                {"Only snow. For a moment it looks like a hallway.", "只有雪花。有那么一瞬间，它看起来像一条走廊。"},
                {"Snow, and very faintly, a laugh track.", "雪花，还有很微弱的罐头笑声。"},
                {"The snow spells nothing. You read it anyway: STAY.", "雪花什么也没拼出来。你还是读出来了：留下。"},
                {"A test card, then a date: SEP. 14 1997.", "一张测试卡，然后是一个日期：1997 年 9 月 14 日。"},
                {"Snow. Behind it, someone is filming this television.", "雪花。雪花后面，有人正在拍这台电视。"},
                {"A weather report for a town you used to know. Fog, all week.", "一个你曾经熟悉的小镇的天气预报。整周都是雾。"},
                {"The channel number is 3:17.", "频道号是 3:17。"},
                {"Snow. It is warm, like breath on glass.", "雪花。是温的，像哈在玻璃上的气。"},
        };
        for (int i = 0; i < snow.length; i++) {
            put("oneirgeo.tv.snow." + i, snow[i][0], snow[i][1]);
        }
        put("subtitles.oneirgeo.block.telephone.ring", "A telephone rings", "电话铃响");
        put("subtitles.oneirgeo.block.telephone.line", "A voice on the line", "电话那头有声音");
        put("subtitles.oneirgeo.block.television.on", "A television hums on", "电视嗡的一声亮了");
        put("subtitles.oneirgeo.tape.static", "Tape hiss", "录像带的沙沙声");
        put("subtitles.oneirgeo.block.clock.tick", "A stopped clock ticks once", "停摆的钟走了一下");
        put("subtitles.oneirgeo.block.monitor.beep", "A monitor beeps", "监护仪滴了一声");
        put("subtitles.oneirgeo.knock", "Three knocks", "三下敲门声");
        put("oneirgeo.dream.asleep", "You are dreaming", "你在做梦");
        put("oneirgeo.dream.faded", "The dream lets go of you", "梦放开了你");

        String[][] whispers = {
                {"you have been here before", "你来过这里"},
                {"wake up", "醒醒"},
                {"it is later than you think", "比你想的要晚"},
                {"are you still there?", "你还在吗？"},
                {"the door was always open", "门一直是开着的"},
                {"nobody is coming back", "没有人会回来了"},
                {"remember the colour of the sky", "记住天空的颜色"},
                {"this is not your room", "这不是你的房间"},
                {"keep walking", "继续走"},
                {"it was smaller when you were small", "你小时候它没这么大"},
                {"everyone left at dusk", "大家都在黄昏时离开了"},
                {"you are almost awake", "你快醒了"},
                {"the light is still on", "灯还亮着"},
                {"don't look up", "别往上看"},
                {"the floor is very far down", "地板在很深很深的下面"},
                {"this place remembers you", "这里记得你"},
                {"count the doors", "数一数门"},
                {"it's okay to stay", "留下来也没关系"},
                {"you dropped something a long time ago", "很久以前你掉了什么东西"},
                {"it's warm here", "这里很暖和"},
                {"who are you?", "你是谁？"},
                {"the sun has been setting for years", "太阳已经落了好多年了"},
                {"listen", "听"},
                {"you were never alone here", "你在这里从来不是一个人"}
        };
        for (int i = 0; i < whispers.length; i++) {
            put("oneirgeo.whisper." + i, whispers[i][0], whispers[i][1]);
        }
        String[][] trapped = {
                {"there is no way out", "没有出口"},
                {"you came in, didn't you?", "是你自己走进来的，不是吗？"},
                {"every door is this door", "每一扇门都是这扇门"},
                {"stay", "留下"}
        };
        for (int i = 0; i < trapped.length; i++) {
            put("oneirgeo.whisper.trap." + i, trapped[i][0], trapped[i][1]);
        }
        put("gamerule.oneirgeo.supply_interval", "Supply interval", "物资发放间隔");
        put("gamerule.oneirgeo.supply_interval.description", "Ticks between two supply deliveries to each player; 0 turns them off.", "两次向每位玩家发放物资之间的刻数，0 表示关闭。");
        put("gamerule.oneirgeo.lucidity", "Lucidity", "清醒度");
        put("gamerule.oneirgeo.lucidity.description", "Whether the hidden lucidity of players drifts.", "玩家隐藏的清醒度是否会变化。");

        put("commands.oneirgeo.not_layered", "This dimension is not a layered Oneirgeo dimension", "这个维度不是梦域的分层维度");
        put("commands.oneirgeo.no_layer", "No layer with that name here", "这里没有这个名字的层");
        put("oneirgeo.config.summary",
                "effects %s, safe mode %s, intensity %s, screen text %s, far silhouettes %s, wrong sky %s, reverb %s, camcorder %s, shake %s",
                "画面效果 %s，光敏安全模式 %s，强度 %s，屏幕文字 %s，远景剪影 %s，错误的天空 %s，混响 %s，录像机 %s，晃动 %s");
        put("oneirgeo.bed.no_sleep", "You cannot fall asleep here", "你在这里睡不着");

        put("biome.oneirgeo.neural_fog", "Neural Fog", "神经雾海");
        put("biome.oneirgeo.hanging_city", "Hanging City", "倒悬之城");
        put("biome.oneirgeo.great_hearth", "Great Hearth", "巨大炉膛");
        put("biome.oneirgeo.ash_plains", "Dead Furnace", "熄灭的熔炉");
        put("biome.oneirgeo.lava_sea", "Lava Sea", "熔岩之海");
        put("biome.oneirgeo.boiler_corridors", "Boiler Corridors", "锅炉房走廊");
        put("biome.oneirgeo.void_geometry", "Void Geometry", "虚空几何");
        put("biome.oneirgeo.star_cemetery", "Star Cemetery", "星空墓园");
        put("biome.oneirgeo.night_sea", "Night Sea", "永夜之海");
        put("biome.oneirgeo.mirror_sea", "Mirror Sea", "镜面之海");
        put("biome.oneirgeo.poolrooms", "Poolrooms", "无尽泳池");
        put("biome.oneirgeo.backrooms", "Backrooms", "后室");
        put("biome.oneirgeo.closed_room", "Closed Room", "封闭的房间");

        put("subtitles.oneirgeo.ambient.dusk", "Dusk wind", "黄昏的风");
        put("subtitles.oneirgeo.ambient.high_wind", "Wind far above", "高处的风");
        put("subtitles.oneirgeo.ambient.rooms", "Rooms breathe", "房间在呼吸");
        put("subtitles.oneirgeo.ambient.rooms.mood", "Something in another room", "隔壁房间有动静");
        put("subtitles.oneirgeo.ambient.furnace", "A furnace long cold", "早已冷却的熔炉");
        put("subtitles.oneirgeo.ambient.furnace.mood", "Metal settles", "金属在沉降");
        put("subtitles.oneirgeo.ambient.hum", "Electric hum", "电流的嗡鸣");
        put("subtitles.oneirgeo.ambient.void", "Silence", "寂静");
        put("subtitles.oneirgeo.ambient.sea", "Still water", "静止的水");
        put("subtitles.oneirgeo.ambient.pool", "Water laps", "水轻拍池边");
        put("subtitles.oneirgeo.ambient.pool.drip", "Water drips", "水滴落下");
        put("subtitles.oneirgeo.ambient.closed", "Your own breathing", "你自己的呼吸");
        put("subtitles.oneirgeo.music.dream", "Music", "音乐");
        put("subtitles.oneirgeo.music.deep", "Music", "音乐");
        put("subtitles.oneirgeo.music.furnace", "Music", "音乐");
        put("subtitles.oneirgeo.music.void", "Music", "音乐");
        put("subtitles.oneirgeo.whisper", "A whisper", "低语");
        put("subtitles.oneirgeo.supply", "Something appears in your pocket", "口袋里多了什么");
        put("subtitles.oneirgeo.passage", "A door gives way", "门开了");
        put("subtitles.oneirgeo.wake", "You wake up", "你醒了");
        put("subtitles.oneirgeo.heal", "The room puts itself back", "房间恢复了原样");
        put("subtitles.oneirgeo.entity.figure", "A figure", "一个身影");
        put("subtitles.oneirgeo.entity.stalker", "Footsteps behind you", "身后的脚步声");
    }

    static FabricLanguageProvider english(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new Provider(output, "en_us", 0, registries);
    }

    static FabricLanguageProvider chinese(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        return new Provider(output, "zh_cn", 1, registries);
    }

    private static final class Provider extends FabricLanguageProvider {
        private final int column;

        Provider(FabricPackOutput output, String code, int column, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, code, registries);
            this.column = column;
        }

        @Override
        public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder builder) {
            STRINGS.forEach((key, values) -> builder.add(key, values[this.column]));
        }
    }
}
