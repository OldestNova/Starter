# Starter
GUI 应用转换控制台启动器，Starter 会在内部使用 Process 启动子进程并且创建 JobObject 以便于控制子进程的生命周期。

所有传递给 Starter 的参数将会被传递给子进程。

Starter 需要一个 `starter-config.json` 文件来配置启动器的行为。

```json
{
  "groupName": "app-group", 
  "directory": ".", 
  "execute": "app.exe",
  "extraArgs": [],
  "hideConsole": false,
  "elevated": false
}
```

- `groupName` 用于标识 JobObject 的名称。唯一即可
- `directory` 用于指定子进程的工作目录，可选，默认为 `.` 即当前目录
- `execute` 用于指定子进程的可执行文件名
- `exreaArgs` 用于添加附加参数，字符串数组，默认为空数组
- `hideConsole` 用于指定是否隐藏控制台窗口，默认为 `false`
- `elevated` 用于指定是否以管理员权限启动子进程，默认为 `false`

## LICENSE
MIT License