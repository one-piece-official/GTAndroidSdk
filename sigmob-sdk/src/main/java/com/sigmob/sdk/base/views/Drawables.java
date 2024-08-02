package com.sigmob.sdk.base.views;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public enum Drawables {


    LOADING("iVBORw0KGgoAAAANSUhEUgAAAHsAAAB7CAYAAABUx/9/AAACfklEQVR42u3dUW7qQAwFUPa/A9bCWlgL/asQEnrolbE9c8+V+Gyi+GDiSVLlcgnL4yUXyYAGHgYNHLbAFtgCW2Bn5n6/P54/p2Jfr9fH8yceejV4F/YrdBz4O+iV4B3Y76CjwGHDXgpejf0vaNgLwSuxP4F23g7CNpEvBK/CBg0bdiV4BTboIcPaamxD2aDunoB9kZruXomtq4d1dzc24ULwVdigYcPuBF+BDXrosPZtbEPZ4O7uwCbZ1N3fxNbVG3e3rn7J7XZ7PH9OAd8Vetk+X6GrwDueaNkBe9l+30FXoe+GvfUX7RPsleiwPx8IS7FXoU+BXv4z+gfkNuwK8O6BdSJ02Xm7a4g7Lf+LXDKRQ+9HXjYYQg9ABh4IDT0MGXogMvBAaMu0DZZT09Bhb36LFHbhte2dwNOxjzpoA1rg40uWXp5RExERERERERERGZy/3lz3b6yb1L1qh8B7oH/rXr1D4D3QsGHDhg17f2wDWtCAZullySsiIiIiIiIiIiJtSb2UF3XcydduY449/WJ9zPG7MxNQA7fhAmrhnmtATdxcD6kP6IA6QQ6oG+SQOoIOqCfkEHTIIcu0KcjTvkRHvuppInQ3+LEvcev+yZ52mjj69Yzd52XYxRbTXh08GXvrVyp3DkgrCvvtvz/uZelTlyBdHWHJ2TCQdJ1rXWPYtKt192Fd3YUNvKljOrYJu6l4XdsFDhv0yqJ1bht48bDTjQ28sDO6tw+7sCsmYMeDVxVowj6isSuLM2U/seCwQS8pyqR9RYF3DDPTsGPAJz202PmwIOyDHlyEDdt5e3UBugpvIn9TiNN+TTqOU/ykwoYNW2ALbIEtYWv7afkBhefbcgmFrrkAAAAASUVORK5CYII="),
    SOUND_MUTE("iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAMAAACdt4HsAAAAPFBMVEUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADQLyYwAAAAE3RSTlMAgEBWq8EoaOgK05qR3hXzonY2pCHkzQAAAVNJREFUWMPt1sluwzAMRVFqlueB//+vrYUYD4oXJVl0U+QtE98TIIkh0z9fDiWTfUPk76X8i75tNApr4teiqc+ptWFk5sHSXyHvRI6Zvb23Audy9RNZAV/vHoC+n8kKhA29HBi8u8fo5UCp3M2RDgj83uuA4dErgczMi7sXMmkB//apfwCsZX8oIQU5EJg5PN7YVjHgGQJeZzlAEQJ6R2IAQtdrAAjoNQAE9CoAAnodAAG9CoCAXgtAcPZ7YcRJYwIctwUDgB6CBkDvIgQVgN8fggZAD0EDoIegA9BDkAPoe0EOePQEocqB8riiCVkM5Gl5XDDxRAJAfDIlf48w29lY56wFqHC35dQCFHuhei1A/kj3LmELPaDagTveAOAbKUYATzyHHSDfBGcH6GzCbAcob5cw2QFa6yXsHjeddsPCryUybsQhYFzCH8K4MMeS6bOf9gXo1jBFNvmRhAAAAABJRU5ErkJggg=="),
    SOUND_ON("iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAMAAACdt4HsAAAAP1BMVEUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACzJYIvAAAAFHRSTlMAgUC/cVgr8OBMEdIbnMiPN7Jkqf9iVGcAAAIXSURBVFjD7ZbblqsgEEQbkavgLfz/t550G20mcCbCPM7Ug8ssrS1UNwT40/cS0shR9/u3hJp9p93LdOjR5w/kn83zEuCQEkODfz4+Pjyv4yuR5228m4ie0B+BACKPRNzyrxbfXeALwFkiyNDgZwAqRCKYj9MYLI+VASg3E2L93u8M+zMAJ8l1qUol1Ah1AOz41Priq9EkFvtLADh8vgGLB8baoQCwhjNhlir8BcBnTUjFcG+rhmWlggKgUjLxYmAtpjwG8/xdKw0DZMrHvZq3SWB/1f08AtLmuRQmtABAzHkfe4uVagCgVkpq0oBacKm3ALjY8qBRR7cAmDBehVuaARCvNh5xOu0AP51D0FiHdgCMV3hYh9AO8NhCVx3WJgB38XAGqjoAC/pOgOgA7GeKAm86AC4H7J0jUD/JIGKI543rKKM9yygPUn8jYU/6dgC18rWL/WQx6a7V6Gg5c0O1AvbEL9LBow2wxmxLU3T6aAG4R2L/sSG5FoBIX/w7VbMFsJFfvvyBd3UekJUvCV0CKH97WZarmvU/182XGQxizdqREshFCbFkqJax7AaWmNjOYdUBiRIspIeXKHC7/g+gE8pDXTxF4+oA9emYxi+pCkDHewdFR4TxHRCEofnfOaoSQVSPuvGjm6NaysP2pOCegqGv5YAFbz3cFg1YSt44hjEO0KKrtwJ0ak5czz5Jk9ID/f3S8Av0DxRSQcrjlRWPAAAAAElFTkSuQmCC"),
    CLOSE_OLD("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"),
    CLOSE("iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAMAAACdt4HsAAAAM1BMVEUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACjBUbJAAAAEHRSTlMAqyjUgcO3FcwGno4dDZZmlq1c/AAAAQZJREFUWMPt1sFuwjAQANHaJIRAaP3/X1tFDZqlUsA7cGSPq8yT4kOcr8+8bYZxSD1fL/fBobVWMv0aBKG2dcqc6NeJAEJ/38LqjNDf/8Rl+dsd5/7+/tTnm3BN9EKgv7BKCPTfrBCOmzA978+sdoRMz1w3YZwe94VVSqDfnwlB9VFI9gjjJiyuj4Lqo1Bdj3CquZ5ZEFQfBdWvDUKuRzjdBNcjHOilQG8E33P69F7w/etvgCB7BN2XgiDPzwjx+43geiPE+8sIsVcC9y+DIHsE2yO4/48o2B7B9gi2R7A9gu0RbI9gewTbI+z1ThjplVDpMwKLhT4jxFeg7xT+B8PQ3RN8Zptfb3gqESeo0NkAAAAASUVORK5CYII="),
    CLOSE_NEW("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"),

    GRAYCLOSE("iVBORw0KGgoAAAANSUhEUgAAAB4AAAAeCAMAAAAM7l6QAAAAJ1BMVEVHcEw3NzdWVlZRUVFVVVXJycnv7++ampqXl5fMzMz////6+vr6+vqCBLknAAAADXRSTlMAARAeMnK8S0p1/+XjZ7RwtQAAAKBJREFUeNqF00EOAyEIBdABrFj1/udtlCk/1KmykfgiJIqXB7GkESJMvuk4zEMoKoMs+O9RFIBuHBp97bv2p5m+smK/5PdcyUvn2tS11fwtT7alzb0gJesMR2LdJQUPmsQYRU3Bc4FPRQyG9woFo/2O9aG4BNUSXMA3BJf1WkqH80Xh7I/T45N0PMn2QffjcBim8yjCVz1/g/Mnwhc0Y+AHZ9YN/vFQrGwAAAAASUVORK5CYII="),
    APPICON("iVBORw0KGgoAAAANSUhEUgAAAEgAAABICAMAAABiM0N1AAAAeFBMVEVHcEwuo5gMKSYvpZpKrKMEEREEEQ8edWUlpJgTQzwhkIY5o5kgfXUhkogfVVA6qZ4voZYtnZM4n5YeamM0k4ompZkln5Qqp5tUs6owqZ0+sKQkm5BJtKkqoJVYurA8p5xmvbV6xr/J5+SW0cu23dr9/f3s9vXb7+2tVwSjAAAAFXRSTlMA/kLq/hooA/0P6kS1JGG8pnDhg75YgU7hAAAEvElEQVR4XqXYiZKqOBQG4EEQAy6g9snCvqj9/m84OZzQIeFOo3d+u5RqU1/9WbS1/9nMYcrGoG3DAf8XckjOOsnhL7FJSY7XPLuFAWNBGGb59Zig9Qk1KdcsKoqikfquwoumKKLsOlkftDnmIQjBGKsKBsCYYEXFQAgR5sd3WyGTNQq0AgBVAZSiAvqVypB6p845B2gUKj4EaKkG8jOO26pzZcCYbrSGKBoCdvVKrZ0kA2AAv0MMIEtcyXfOITAGWxCOCc9WWjtHgXW2IWzNjlZaOY1iYkqjBKUqBKWoBEU1AqMHWmnl9I1J3/kXXb++IGm9PkJRke1G5iKgdfKcJATWCIA31ogiGnHDvfOhDJj4EGLwhZDrXIHBp5Bg/EqSdc7sryAoaZkslGvncwjcyeHOo/MXEAB3z0C2hIQqfoO6QlnIVLJHkWLO2vAafjmQw+uxHC2pEkG5EgrTKUzzfD4fhZoiK0WppHkc9LPVYnSQI2TPop01qG+U1Dw1oQExT009tPMt7GgG6Z4kc4YWEHRW6vrxgRn7Dp1icipnNL/+QJkHQTNJRTUOr+ec19BX5Eiw0LTc8/tuEvmQ6BEYNOfke0CwFx6UJvOeFai456iziIc17jkyR4mWqGArqBr+AxoqH2JwJeiQ/wGyjp/HGsppkQ5ZU5n05rGzzjpD542Wmdm1W1OYdPTg9/ElOY82udGniyT0pzY+f8/o7BoToT6S+nYOPKh5bqVZQKChs4Hc7S/miT3Gb3fnx/mZoXCgNv4jNM5OXUZL6SXL+mGkkfvQempqrjGWZbuEhrYsx7lc4Ewtng7S3l3s7jX3j+r+uWzUBz8NXx1fQnt6qd2WkHjYDtjHoV524ziHH+iWEJQtz1GFg7fzitq2K0wyA+XLk90930sXReaERzJP6LXmvGjfhngtawY6rL4fJihx3kb6N6EeIT5J9cW8aPfh4o1tfBMaJ0hLDNJ4/pKQL6D+gRkef459jhppicEpmaHrAmIlJqpLStSaizYyF4HkFII443cNkRQv/xwxB2qXEMdb7UE0M2duBAH7LwjDud+InxIL7RSlo4e6lkE9UXXU1pQ2wvsgCFoZUFSj8L68EET7ljl/+6XsZETpmkhOt66TJr178bUnhypdaufNv5beYnM9NU5rFNCMdLumrjlQIVvp5H7Qks4acVojzrm/RoyfqJBdpdL76LduNDELSEjgkO6okK105y7EbCN0EELHbcTKOxWySeKT//H4pxGfGxmonhvpicUJAs7k4tT/wM4XjbidGkKANwlpjBNzk+x3qdMIJduIrxvVTbrbUyFPupT+d5HaOZAUhYcVasFUdCHHy0FLnf/tSM7n0F40fSMl/lhnJcWXVAgwjShRyzFOIyzEIL3YBVpLu5R7X0VbetF7a8R4unMdf3a7UwmaslBBm2UhJTmD8rTbe44vxfeUA5MWAgIMBLoR8PQe++vjR0tYyvkHgoUwQuo6mw6VupyiAJgOQWAhxsvy6xKbaW1LmkpLAI1VBYIE6YcyPV1irIPQdpDa3bXFgb4cA+jtL7Vy38Vune1WaF3up6/b7RaG+u7rdL+g4jHbFFlao2iDFGI+tTQ2cXtMgop1PuYwbxT5F5RDYLHX81i6AAAAAElFTkSuQmCC"),

    BACK("iVBORw0KGgoAAAANSUhEUgAAACAAAAAgBAMAAACBVGfHAAAAG1BMVEUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACUUeIgAAAACHRSTlMAgFZAdWRsIFzMPMEAAABASURBVCjPY6AIMBegCWgooPKZGtEVCAw+BcxwBQgBTC0EDGWIQFfCSlgJG4aSDHQlLBhKPAgrkTBA95EBmTEMANsTClin7SK5AAAAAElFTkSuQmCC"),

    SKIP_ICON("iVBORw0KGgoAAAANSUhEUgAAACAAAAA4BAMAAABu0ecxAAAAHlBMVEUAAAD///////////////////////////////////8kfJuVAAAACXRSTlMAYC/uh9G4SCWT8wQBAAAAVElEQVQoz2PAAEwuCqgClTMnogpEzpwpgCJgORNNSeZMNCXNM9GUMFmiK1EeVUKCEgV0JQ4YAhha0A3FsHZUAVwBZsLFTNqIxI+RPdAzEGYWoxAAAHuRcKlAva7oAAAAAElFTkSuQmCC");

    private final String encodedString;
    private Bitmap cachedBitmap;

    Drawables(final String encodedString) {
        this.encodedString = encodedString;
    }

    public Bitmap getBitmap() {
        if (cachedBitmap == null) {
            byte[] rawImageData = Base64.decode(encodedString, Base64.DEFAULT);
            cachedBitmap = BitmapFactory.decodeByteArray(rawImageData, 0, rawImageData.length);
        }
        return cachedBitmap;
    }

}
