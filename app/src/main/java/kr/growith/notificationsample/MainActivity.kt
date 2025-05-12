package kr.growith.notificationsample

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kr.growith.notificationsample.ui.theme.NotificationSampleTheme
import android.Manifest
import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment



class MainActivity : ComponentActivity() {
    var builder : NotificationCompat.Builder? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val notificationPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {
                // if permission was denied, the service can still run only the notification won't be visible
            }

         //1. Notification 채널 생성
         fun createNotificationChannel(channelId:String) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is not in the Support Library.
             val channelName = "name"
             val channelDescriptionText = "notification1"

             val importance = NotificationManager.IMPORTANCE_DEFAULT
             val channel = NotificationChannel(channelId, channelName, importance).apply {
                 description = channelDescriptionText
             }
             // Register the channel with the system.
             val notificationManager: NotificationManager =
                 getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
             notificationManager.createNotificationChannel(channel)
         }


        //2. PendingIntent 생성 - 사용자가 Notification을 클릭하면 이동 할 Activity의 Intent 생성
        fun makeIntent(activity:Class<out Activity>) : PendingIntent{
            val intent = Intent(this, activity).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            return pendingIntent
        }


        //3. NotificationBuilder 생성
        fun getNotificationBuilder(channelId:String,pendingIntent: PendingIntent) : NotificationCompat.Builder? {

            //Notification에 표시할 제목과 내용
            val notificationTitle ="My notification title"
            val notificationText = "Hello World!"

            builder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            return builder
        }


        //3. 실제 알림을 발송.
        fun notifyNotification( builder : NotificationCompat.Builder,notificationId:Int ){
            with(NotificationManagerCompat.from(this)) {

                //Notification 발송 전 권한체크 (필수).
                if (ActivityCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Android TIRAMISU 이상인 경우 권한 요청. (이전 버전은 권한이 필요 없음)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }else{
                        notify(notificationId, builder.build())
                    }
                    return@with
                }
                // notificationId is a unique int for each notification that you must define.
                notify(notificationId, builder.build())
            }
        }
        enableEdgeToEdge()
        setContent {
            val channelId = "1"
            val notificationId = 1

            createNotificationChannel(channelId)
            val pendingIntent = makeIntent(MainActivity::class.java)
            val builder = getNotificationBuilder(channelId,pendingIntent)

            NotificationSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NotificationSampleScreen(
                        modifier = Modifier.padding(innerPadding), onClick = {
                            builder?.let { notificationBuilder ->
                                notifyNotification(notificationBuilder,notificationId)
                            }
                        }
                    )
                }
            }
        }
    }
}




@Composable
fun NotificationSampleScreen( modifier: Modifier = Modifier, onClick : () -> Unit = {}) {
    Box(modifier= modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Button(onClick = onClick) { Text("Send My Notification!") }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NotificationSampleTheme {
        NotificationSampleScreen()
    }
}