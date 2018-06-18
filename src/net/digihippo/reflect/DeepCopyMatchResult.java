package net.digihippo.reflect;

public final class DeepCopyMatchResult
{
    public final boolean isDeepCopy;
    public final String failureDescription;

    public DeepCopyMatchResult(boolean isDeepCopy, String failureDescription)
    {
        this.isDeepCopy = isDeepCopy;
        this.failureDescription = failureDescription;
    }

    public static DeepCopyMatchResult failure(String failureMessage)
    {
        return new DeepCopyMatchResult(false, failureMessage);
    }

    public static DeepCopyMatchResult success()
    {
        return new DeepCopyMatchResult(true, null);
    }
}
