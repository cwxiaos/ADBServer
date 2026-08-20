importClass(android.app.ActivityOptions)
importClass(android.content.Intent)
importClass(android.graphics.Rect)

const CONFIG = [
    {
        'name': 'com.netease.sky.bilibili',
        'activity': 'com.tgc.sky.netease.GameActivity_Netease',
        'left': 100,
        'top': 10,
        'width': 890,
        'height': 600,
        'offset_x': 0,
        'offset_y': 0,
    },
    {
        'name': 'com.netease.sky.huawei',
        'activity': 'com.tgc.sky.netease.GameActivity_Netease',
        'left': 100,
        'top': 750,
        'width': 890,
        'height': 600,
        'offset_x': -50,
        'offset_y': -100,
    }
]

function runADBShell(cmd) {
    var Array = java.lang.reflect.Array
    var args = ["shell", cmd]
    var strArray = Array.newInstance(java.lang.String, args.length)
    for (var i = 0; i < args.length; i++) {
        Array.set(strArray, i, args[i])
    }
    var intent = new Intent()
    intent.setClassName("com.termux", "com.termux.app.RunCommandService")
    intent.setAction("com.termux.RUN_COMMAND")
    intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/adb")
    intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", strArray)
    intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)
    context.startForegroundService(intent)
}

function launchFreeformADB(pkg, activity, left, top, right, bottom) {
    var cmd =
        "am start -n " + pkg + "/" + activity + " --windowingMode 5;" +
        " sleep 1.5;" +
        " TID=$(dumpsys activity activities | grep -A 3 '" + pkg + "' | grep -oE 'taskId=[0-9]+' | head -1 | cut -d= -f2);" +
        " am task resize $TID " + left + " " + top + " " + right + " " + bottom
    runADBShell(cmd)
}

function stopAppADB(pkg) {
    runADBShell("am force-stop " + pkg)
}

function launchFreeform(pkg, activity, left, top, right, bottom) {
    var options = ActivityOptions.makeBasic();
    try {
        var m = ActivityOptions.class.getMethod("setLaunchWindowingMode", java.lang.Integer.TYPE)
        m.invoke(options, java.lang.Integer.valueOf(5)) // WINDOWING_MODE_FREEFORM = 5
        options.setLaunchWindowingMode(5)
    } catch (e) {
        log("setLaunchWindowingMode failed:" + e)
    }

    options.setLaunchBounds(new Rect(left, top, right, bottom))

    var intent = new Intent()
    intent.setClassName(pkg, activity)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

    context.startActivity(intent, options.toBundle())
}

function stopApp(pkg) {
    var intent = new Intent();

    var Array = java.lang.reflect.Array
    var args =  ["shell", "am", "force-stop", pkg]

    var strArray = Array.newInstance(java.lang.String, args.length)
    for (var i = 0; i < args.length; i++) {
        Array.set(strArray, i, args[i])
    }
    
    intent.setClassName("com.termux", "com.termux.app.RunCommandService")
    intent.setAction("com.termux.RUN_COMMAND")
    intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/adb")
    intent.putExtra(
        "com.termux.RUN_COMMAND_ARGUMENTS",
        strArray
    )
    intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", true);
    context.startForegroundService(intent);
}

function closeApp(left, top, close) {
    sleep(100)
    click(left + 835, top + close)
}

function startGame(left, top) {
    sleep(100)
    click(left + 690, top + 300)
}

function continueAdventure(left, top) {
    sleep(100)
    gestures([800, [left + 200, top + 480], [left + 200, top + 320]])
    sleep(200)
    gestures([800, [left + 200, top + 480], [left + 200, top + 320]])
    sleep(200)
    gestures([800, [left + 200, top + 480], [left + 200, top + 320]])
    sleep(1000)
    click(left + 290, top + 440)
    sleep(1000)
    click(left + 580, top + 480)
    sleep(1000)
    click(left + 580, top + 480)
    sleep(1000)
}

function moveToTarget(left, top) {
    sleep(100)
    // click(left + 590, top + 410)
    gestures([800, [left + 200, top + 480], [left + 200, top + 320]])
    sleep(400)
    
    sleep(1000)
    gestures([800, [left + 730, top + 350], [left + 600, top + 350]])
    
    for (let i = 0; i < 11; i ++) {
        sleep(500)
        gestures([800, [left + 200, top + 480], [left + 200, top + 320]])
    }
    
    sleep(500)
    gestures([800, [left + 730, top + 350], [left + 510, top + 350]])
    
    for (let i = 0; i < 4; i ++) {
        sleep(500)
        gestures([800, [left + 100, top + 480], [left + 100, top + 320]])
    }

    //sleep(500)
    //gestures([800, [left + 730, top + 350], [left + 550, top + 350]])
    
    //sleep(100)
    //click(left + 590, top + 410)
}

function skipAD(configs) {
    for (let i = 0; i < 5; i++) {
        for (let j = 0; j < configs.length; j++) {
            launchFreeformADB(
                configs[j]['name'],
                configs[j]['activity'],
                configs[j]['left'],
                configs[j]['top'],
                configs[j]['left'] + configs[j]['width'],
                configs[j]['top'] + configs[j]['height']
            )
            sleep(400)
        }
        sleep(20 * 1000)
        
        for (let j = 0; j < configs.length; j++) {
            stopAppADB(configs[j]['name'])
            sleep(400)
        }
        sleep(3000)
    }
}

function startSingleGame(config) {
    launchFreeform(config['name'], config['activity'], config['left'], config['top'], config['left'] + config['width'], config['top'] + config['height'])
    sleep(20 * 1000)

    startGame(config['left'] + config['offset_x'], config['top'] + config['offset_y'])
    sleep(35 * 1000)
    continueAdventure(config['left'] + config['offset_x'], config['top'] + config['offset_y'])
    sleep(10 * 1000)
    moveToTarget(config['left'] + config['offset_x'], config['top'] + config['offset_y'])
}

//skipAD(CONFIG)
startSingleGame(CONFIG[1])

//sleep(20 * 1000)

for (let i = 5; i < CONFIG.length; i++) {
    startSingleGame(CONFIG[i])
    sleep(1000)
}